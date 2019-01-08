#!/bin/bash

source launch_diff_variables.sh

if [ -z "$1" ]; then
	echo "No parameter supplied!"
	echo "      Either supply the PR branch and the validation profile to work with or 'clean'."
	echo ""
	exit 1
fi

if [ "$1" == "clean" ] || [ "$1" == "-clean" ]; then
	echo "Cleaning..."

	cd $CHECKSTYLE_DIR
	mvn --batch-mode clean
	rm -rf $FINAL_RESULTS_DIR/*
	exit 0
fi

if [ -z "$2" ]; then
	echo "No 2nd parameter supplied!"
	echo "      Please supply the validation profile to work with."
	echo ""
	exit 1
fi

function parse_arguments {
	SKIP=2

	while [[ $# > 0 ]] ; do
		if [ $SKIP == 0 ] ; then
			case "$1" in
			-output)
				FINAL_RESULTS_DIR=$2
				shift
				;;
			*)
				echo "Unknown option: $1"
				exit 1
				;;
			esac
		else
			SKIP=$((SKIP--))
		fi
		shift
	done
}

function run_mvn_pitest {
	echo "Running pitest $1"

	mvn_pitest $1

	if [ ! -d "target/pit-reports" ]; then
		echo "Pitest report doesn't exist"
		exit 1
	fi
	if [ -z "$(ls -A target/pit-reports)" ]; then
		echo "Pitest directory is empty"
		exit 1
	fi

	cd target/pit-reports/
	cd *

	if [ -z "$(ls -A)" ]; then
		echo "Pitest report directory is empty"
		exit 1
	fi

	if [ ! -d "$2" ]; then
		mkdir $2
	else
		rm -rf $2/*
	fi

	mv * $2

	cd ../
	rm -rf *
	cd ../../
}

function mvn_pitest {
	echo "mvn --batch-mode -P$1,no-validations verify org.pitest:pitest-maven:mutationCoverage -DargLine='-Xmx3900m' -Dmaven.main.skip=true -Dmaven.test.skip=true -Dmaven.site.skip=true"
	export MAVEN_OPTS="-Xmx3900m"
	mvn --batch-mode -P$1,no-validations verify org.pitest:pitest-maven:mutationCoverage -DargLine='-Xmx3900m' -Dmaven.main.skip=true -Dmaven.test.skip=true -Dmaven.site.skip=true

	# command can fail, but reports are still generated
}

function getReportInfo {
	points=($(grep -F 'class="coverage_legend">' $1 | sed -e 's/.*legend">\([0-9]\+\/[0-9]\+\)<\/.*/\1/' | awk '!seen[$0]++'))

	if [ "${#points[@]}" == "0" ]; then
		echo ""
	else
		coveragePoints=($(sed 's/\//\n/g' <<< ${points[0]}))
		mutationPoints=($(sed 's/\//\n/g' <<< ${points[1]}))
		coverageCount=$((${coveragePoints[1]} - ${coveragePoints[0]}))
		mutationCount=$((${mutationPoints[1]} - ${mutationPoints[0]}))

		if [ "$coverageCount" != "0" ] || [ "$mutationCount" != "0" ]; then
			echo " ($coverageCount, $mutationCount)"
		else
			echo ""
		fi
	fi
}

# ============================================================
# ============================================================
# ============================================================

parse_arguments "$@"

echo "Testing Checkstyle Starting"

cd $CHECKSTYLE_DIR

echo "Checking out and Running PR $1"

git fetch $PULL_REMOTE

if [ ! `git rev-parse --verify $PULL_REMOTE/$1` ] ;
then
	echo "Branch $PULL_REMOTE/$1 doesn't exist"
	exit 1
fi

git reset --hard HEAD
git checkout $PULL_REMOTE/$1
git clean -f -d

echo "Compiling PR $1"

mvn clean verify test -Pno-validations

if [ $? -ne 0 ]; then
	echo "Maven Compile Failed!"
	exit 1
fi

if [ ! -d "$FINAL_RESULTS_DIR" ]; then
	mkdir $FINAL_RESULTS_DIR
else
	rm -rf $FINAL_RESULTS_DIR/*
fi

if [ "$2" == "pitest--all" ]; then
	reports=(pitest-misc pitest-annotation pitest-blocks pitest-coding pitest-design pitest-header pitest-imports pitest-indentation pitest-javadoc pitest-metrics pitest-modifier pitest-naming pitest-regexp pitest-sizes pitest-whitespace pitest-ant pitest-packagenamesloader pitest-common pitest-common-2 pitest-main pitest-tree-walker pitest-api pitest-filters pitest-utils pitest-gui pitest-xpath)

	for report in "${reports[@]}"
	do
		run_mvn_pitest $report $FINAL_RESULTS_DIR/$report
	done

	echo "Starting all Reports"

	OUTPUT_FILE="$FINAL_RESULTS_DIR/index.html"
	if [ -f $OUTPUT_FILE ] ; then
		rm $OUTPUT_FILE
	fi
	echo "<html><body>" >> $OUTPUT_FILE
	echo "<h3><span style=\"color: #ff0000;\">" >> $OUTPUT_FILE
	echo "</span></h3>" >> $OUTPUT_FILE

	REMOTE="$PULL_REMOTE/$1"
	HASH=$(git rev-parse $REMOTE)
	MSG=$(git log $REMOTE -1 --pretty=%B)

	echo "<h6>" >> $OUTPUT_FILE
	echo "Patch branch: $REMOTE<br />" >> $OUTPUT_FILE
	echo "Patch branch last commit SHA: $HASH<br />" >> $OUTPUT_FILE
	echo "Patch branch last commit message: $MSG<br />" >> $OUTPUT_FILE
	echo "</h6>" >> $OUTPUT_FILE

	for report in "${reports[@]}"
	do
		echo "<a href='$report/index.html'>$report</a>" >> $OUTPUT_FILE
		echo $(getReportInfo $FINAL_RESULTS_DIR/$report/index.html) >> $OUTPUT_FILE
		echo "<br />" >> $OUTPUT_FILE
	done

	echo "</body></html>" >> $OUTPUT_FILE
else
	run_mvn_pitest $2 $FINAL_RESULTS_DIR
fi

echo "Complete"

exit 0
