#!/bin/bash

source launch_diff_variables.sh

EXTPROJECTS=()
USE_CUSTOM_MASTER=false
CUSTOM_MASTER=""

if [ -z "$1" ]; then
	echo "No parameter supplied!"
	echo "      Either supply the PR branch to work with or 'clean'."
	echo ""
	exit 1
fi

if [ "$1" == "clean" ] || [ "$1" == "-clean" ]; then
	echo "Cleaning..."

	cd $CHECKSTYLE_DIR
	mvn --batch-mode clean
	cd $TESTER_DIR
	rm -rf $SITE_SAVE_MASTER_DIR
	rm -rf $SITE_SAVE_PULL_DIR
	rm -rf $FINAL_RESULTS_DIR/*
	exit 0
fi

function parse_arguments {
	SKIP=true

	while [[ $# > 0 ]] ; do
		if ! $SKIP ; then
			case "$1" in
			-master)
				USE_CUSTOM_MASTER=true
				CUSTOM_MASTER=$2
				shift
				;;
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
			SKIP=false
		fi
		shift
	done
}

function mvn_package {
	echo "mvn --batch-mode -Pno-validations clean package -Passembly"
	mvn --batch-mode -Pno-validations clean package -Passembly

	if [ $? -ne 0 ]; then
		echo "Maven Package Failed!"
		exit 1
	fi

	mv target/checkstyle-*-all.jar $TEMP_DIR/checkstyle-$1-all.jar
}

function launch {
		if [ ! -d "$1" ]; then
			mkdir $1
		fi
		if [ ! -d "$2" ]; then
			mkdir $2
		fi

		while read line ; do
			[[ "$line" == \#* ]] && continue # Skip lines with comments
			[[ -z "$line" ]] && continue     # Skip empty lines
			
			REPO_NAME=`echo $line | cut -d '|' -f 1`
			REPO_TYPE=`echo $line | cut -d '|' -f 2`
			REPO_URL=` echo $line | cut -d '|' -f 3`
			COMMIT_ID=`echo $line | cut -d '|' -f 4`
			EXCLUDES=` echo $line | cut -d '|' -f 5`
			
			echo "Running Launches on $REPO_NAME ..."

			if [ ! -d "$REPOSITORIES_DIR" ]; then
				mkdir $REPOSITORIES_DIR
			fi
			REPO_SOURCES_DIR=
			
			if [ "$REPO_TYPE" == "git" ]; then
				GITPATH=$REPOSITORIES_DIR/$REPO_NAME
				
				if [ ! -d "$GITPATH" ]; then
					echo "Cloning $REPO_TYPE repository '${REPO_NAME}' ..."
					git clone $REPO_URL $GITPATH
					echo -e "Cloning $REPO_TYPE repository '$REPO_NAME' - completed"
				fi
				if [ "$COMMIT_ID" != "" ] && [ "$COMMIT_ID" != "master" ]; then
					echo "Reseting $REPO_TYPE sources to commit '$COMMIT_ID'"
					cd $GITPATH
					if $CONTACTSERVER ; then
						git fetch origin
					fi
					git reset --hard $COMMIT_ID
					git clean -f -d
					cd -
				else
					echo "Reseting GIT $REPO_TYPE sources to head"
					cd $GITPATH
					if $CONTACTSERVER ; then
						git fetch origin
					fi
					git reset --hard origin/master
					git clean -f -d
					cd -
				fi

				REPO_SOURCES_DIR=$GITPATH
			elif [ "$REPO_TYPE" == "hg" ]; then
				HGPATH=$REPOSITORIES_DIR/$REPO_NAME

				if [ ! -d "$HGPATH" ]; then
					echo "Cloning $REPO_TYPE repository '${REPO_NAME}' ..."
					hg clone $REPO_URL $HGPATH
					echo -e "Cloning $REPO_TYPE repository '$REPO_NAME' - completed"
				fi
				if [ "$COMMIT_ID" != "" ] && [ "$COMMIT_ID" != "master" ]; then
					echo "Reseting HG $REPO_TYPE sources to commit '$COMMIT_ID'"
					cd $HGPATH
					hg up $COMMIT_ID
					cd -
				fi

				REPO_SOURCES_DIR=$HGPATH
			else
				echo "Unknown RepoType: $REPO_TYPE"
				exit 1
			fi

			if [ -z "$REPO_SOURCES_DIR" ] || [ ! -d "$REPO_SOURCES_DIR" ]; then
				echo "Unable to find RepoDir for $REPO_NAME: $REPO_SOURCES_DIR"
				exit 1
			fi

			SECONDS=0
			echo "Running Checkstyle on all files in $SITE_SOURCES_DIR"

			for f in $(find $REPO_SOURCES_DIR -name '*.java')
			do
				result=$()
echo "$f"
				saveMasterFile=${f#$REPO_SOURCES_DIR/}
				saveMasterFile=${saveMasterFile%".java"}
				saveMasterFile=$1/$REPO_NAME/$saveMasterFile.tree
				saveMasterDir=$(dirname "$saveMasterFile")

				if [ ! -d "$saveMasterDir" ]; then
					mkdir -p $saveMasterDir
				fi

				savePatchFile=${f#$REPO_SOURCES_DIR/}
				savePatchFile=${savePatchFile%".java"}
				savePatchFile=$2/$REPO_NAME/$savePatchFile.tree
				savePatchDir=$(dirname "$savePatchFile")

				if [ ! -d "$savePatchDir" ]; then
					mkdir -p $savePatchDir
				fi

				# parallel run
				java -jar $TEMP_DIR/checkstyle-master-all.jar -J $f > $saveMasterFile 2>&1 &
				java -jar $TEMP_DIR/checkstyle-patch-all.jar -J $f > $savePatchFile 2>&1 &
				wait
			done

			duration=$SECONDS
			echo "Running Checkstyle on $SITE_SOURCES_DIR - finished - $(($duration / 60)) minutes and $(($duration % 60)) seconds."

			if ! containsElement "$REPO_NAME" "${EXTPROJECTS[@]}" ; then
				EXTPROJECTS+=($REPO_NAME)
			fi

			echo "Running Launch on $REPO_NAME - completed"
		done < $TESTER_DIR/projects-to-test-on.properties
}

function containsElement {
	local e
	for e in "${@:2}";
	do
		[[ "$e" == "$1" ]] && return 0;
	done
	return 1
}

# ============================================================
# ============================================================
# ============================================================

parse_arguments "$@"

if [ ! -d "$TEMP_DIR" ]; then
	mkdir $TEMP_DIR
fi

echo "Testing Checkstyle Starting"

if $CONTACTSERVER ; then
	echo "with server updates enabled"
fi

cd $CHECKSTYLE_DIR

if $USE_CUSTOM_MASTER ; then
	git fetch $PULL_REMOTE

	if [ ! `git rev-parse --verify $PULL_REMOTE/$CUSTOM_MASTER` ] ;
	then
		echo "Branch $PULL_REMOTE/$CUSTOM_MASTER doesn't exist"
		exit 1
	fi

	git checkout $PULL_REMOTE/$CUSTOM_MASTER
else
	if $CONTACTSERVER ; then
		git fetch origin
	fi

	git reset --hard HEAD
	git checkout origin/master
fi

git clean -f -d

echo "Packaging Master"

mvn_package "master"

echo "Checking out and Installing PR $1"

git fetch $PULL_REMOTE

if [ ! `git rev-parse --verify $PULL_REMOTE/$1` ] ;
then
	echo "Branch $PULL_REMOTE/$1 doesn't exist"
	exit 1
fi

git checkout $PULL_REMOTE/$1
git clean -f -d

mvn_package "patch"

echo "Starting all Launchers"

rm -rf $SITE_SAVE_MASTER_DIR
rm -rf $SITE_SAVE_PULL_DIR

launch $SITE_SAVE_MASTER_DIR $SITE_SAVE_PULL_DIR

echo "Starting all Reports"

if [ ! -d "$FINAL_RESULTS_DIR" ]; then
	mkdir $FINAL_RESULTS_DIR
else
	rm -rf $FINAL_RESULTS_DIR/*
fi

OUTPUT_FILE="$FINAL_RESULTS_DIR/index.html"

if [ -f $OUTPUT_FILE ] ; then
	rm $OUTPUT_FILE
fi
echo "<html><head>" >> $OUTPUT_FILE
echo "<link rel='icon' href='https://checkstyle.org/images/favicon.png' type='image/x-icon' />" >> $OUTPUT_FILE
echo "<title>Checkstyle Tester Report Diff Summary</title>" >> $OUTPUT_FILE
echo "</head><body>" >> $OUTPUT_FILE

if $USE_CUSTOM_MASTER ; then
	REMOTE="$PULL_REMOTE/$CUSTOM_MASTER"
else
	REMOTE="origin/master"
fi

cd $CHECKSTYLE_DIR
HASH=$(git rev-parse $REMOTE)
MSG=$(git log $REMOTE -1 --pretty=%B)

echo "<h6>" >> $OUTPUT_FILE
echo "Base branch: $REMOTE<br />" >> $OUTPUT_FILE
echo "Base branch last commit SHA: $HASH<br />" >> $OUTPUT_FILE
echo "Base branch last commit message: $MSG<br />" >> $OUTPUT_FILE
echo "</h6>" >> $OUTPUT_FILE

REMOTE="$PULL_REMOTE/$1"

cd $CHECKSTYLE_DIR
HASH=$(git rev-parse $REMOTE)
MSG=$(git log $REMOTE -1 --pretty=%B)

echo "<h6>" >> $OUTPUT_FILE
echo "Patch branch: $REMOTE<br />" >> $OUTPUT_FILE
echo "Patch branch last commit SHA: $HASH<br />" >> $OUTPUT_FILE
echo "Patch branch last commit message: $MSG<br />" >> $OUTPUT_FILE
echo "</h6>" >> $OUTPUT_FILE

echo "Tested projects: ${#EXTPROJECTS[@]}" >> $OUTPUT_FILE
echo "<br /><br /><br />" >> $OUTPUT_FILE

for extp in "${EXTPROJECTS[@]}"
do
	if [ ! -d "$FINAL_RESULTS_DIR/$extp" ]; then
		parentDir=$(dirname "$SITE_SAVE_MASTER_DIR")

		echo "java -jar $DIFF_JAR --compareMode text --baseReport $SITE_SAVE_MASTER_DIR/$extp --patchReport $SITE_SAVE_PULL_DIR/$extp --output $FINAL_RESULTS_DIR/$extp -refFiles $parentDir"

		java -jar $DIFF_JAR --compareMode text --baseReport $SITE_SAVE_MASTER_DIR/$extp --patchReport $SITE_SAVE_PULL_DIR/$extp --output $FINAL_RESULTS_DIR/$extp -refFiles $parentDir

		if [ "$?" != "0" ]
		then
			echo "patch-diff-report-tool failed on $extp"
			exit 1
		fi
	else
		echo "Skipping patch-diff-report-tool for $extp"
	fi

	total=($(grep -Eo 'totalDiff">[0-9]+' $FINAL_RESULTS_DIR/$extp/index.html | grep -Eo '[0-9]+'))

	echo "<a href='$extp/index.html'>$extp</a>" >> $OUTPUT_FILE
	if [ ${#total[@]} != "0" ] ; then
		if [ ${total[0]} -ne 0 ] ; then
			echo " (${total[0]})" >> $OUTPUT_FILE
		fi
	fi
	echo "<br />" >> $OUTPUT_FILE
done

echo "</body></html>" >> $OUTPUT_FILE

echo "Complete"

exit 0
