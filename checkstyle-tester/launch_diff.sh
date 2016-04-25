#!/bin/bash

# ============================================================
# Custom Options
# Note: Use full paths
# ============================================================

MINIMIZE=true
CONTACTSERVER=true

CHECKSTYLE_DIR=~/opensource/checkstyle
TESTER_DIR=~/opensource/contribution/checkstyle-tester
EXTRA_DIR=~/opensource/downloads
FINAL_RESULTS_DIR=~/opensource/results
DIFF_JAR=~/opensource/patch-diff-report-tool.jar

# Note: Full paths no longer needed

PULL_REMOTE=pull

SITE_SOURCES_DIR=src/main/java
SITE_SAVE_MASTER_DIR=savemaster
SITE_SAVE_PULL_DIR=savepull
SITE_SAVE_REF_DIR=saverefs

# ============================================================
# ============================================================
# ============================================================

EXTPROJECTS=()
INSTALL_MASTER=true
RUN_MASTER=true
INSTALL_PULL=true
RUN_PULL=true

if [ -z "$1" ]; then
	echo "No parameter supplied!"
	echo "      Either supply the PR branch to work with or 'clean'."
	echo ""
	exit 1
fi

if [ "$1" == clean ]; then
	echo "Cleaning..."

	cd $CHECKSTYLE_DIR
	mvn --batch-mode clean
	cd $TESTER_DIR
	rm -rf $SITE_SOURCES_DIR/*
	rm -rf $SITE_SAVE_MASTER_DIR
	rm -rf $SITE_SAVE_PULL_DIR
	rm -rf $SITE_SAVE_REF_DIR
	mvn --batch-mode clean
	rm -rf $FINAL_RESULTS_DIR/*
	exit 0
fi

function parse_arguments {
	SKIP=true

	while [[ $@ > 0 ]] ; do
		if $SKIP ; then
			case "$1" in
			-skip)
				case "$2" in
				install_master)
					INSTALL_MASTER=false
					;;
				master)
					INSTALL_MASTER=false
					RUN_MASTER=false
					;;
				install_pull)
					INSTALL_PULL=false
					;;
				pull)
					INSTALL_PULL=false
					RUN_PULL=false
					;;
				esac
				shift
				;;
			esac
		else
			SKIP=false
		fi
		shift
	done
}

function mvn_install {
	mvn --batch-mode clean install -Dmaven.test.skip=true -Dcheckstyle.ant.skip=true -Dcheckstyle.skip=true -Dpmd.skip=true -Dfindbugs.skip=true -Dcobertura.skip=true

	if [ $? -ne 0 ]; then
		echo "Maven Install Failed!"
		exit 1
	fi
}

function launch {
		cd $TESTER_DIR

		echo "Verifying Launch config ..."

		CS_VERSION="grep 'SNAPSHOT</version>' $CHECKSTYLE_DIR/pom.xml | tail -1 | cut -d '>' -f2 | cut -d '<' -f1"
		CS_VERSION="$(eval $CS_VERSION)"
		TEST_VERSION="grep 'SNAPSHOT</checkstyle.version>' pom.xml | tail -1 | cut -d '>' -f2 | cut -d '<' -f1"
		TEST_VERSION="$(eval $TEST_VERSION)"

		echo "Config version: $CS_VERSION vs $TEST_VERSION"
		if [ "$CS_VERSION" != "$TEST_VERSION" ]; then
			echo "Config version mis-match"
			exit 1
		fi

		while read line ; do
			rm -rf $SITE_SOURCES_DIR/*
			
			[[ "$line" == \#* ]] && continue # Skip lines with comments
			[[ -z "$line" ]] && continue     # Skip empty lines
			
			REPO_NAME=`echo $line | cut -d '|' -f 1`
			REPO_TYPE=`echo $line | cut -d '|' -f 2`
			REPO_URL=` echo $line | cut -d '|' -f 3`
			COMMIT_ID=`echo $line | cut -d '|' -f 4`
			EXCLUDES=` echo $line | cut -d '|' -f 5`
			
			echo "Running Launch on $REPO_NAME ..."
			
			REPO_SOURCES_DIR=$SITE_SOURCES_DIR/$REPO_NAME
			
			if [ ! -d "$EXTRA_DIR" ]; then
				mkdir $EXTRA_DIR
			fi
			
			if [ "$REPO_TYPE" == "github" ]; then
				TARNAME=$(echo $REPO_URL | sed -E 's/\//-/')
				TARPATH=$EXTRA_DIR/$TARNAME".tar.gz"
				
				if [ ! -f "$TARPATH" ]; then
					echo "Requesting a tar: 'wget https://api.github.com/repos/$REPO_URL/tarball/$COMMIT_ID -O $TARPATH'"
					wget https://api.github.com/repos/$REPO_URL/tarball/$COMMIT_ID -O $TARPATH
				fi
				if [ ! -d "$REPO_SOURCES_DIR" ]; then
					echo -e "untar $TARNAME file to $REPO_SOURCES_DIR ..."
					tar -xf $TARPATH -C $SITE_SOURCES_DIR
					echo -e "untar $TARNAME file to $REPO_SOURCES_DIR - completed"
				fi
			elif [ "$REPO_TYPE" == "git" ]; then
				GITPATH=$EXTRA_DIR/$REPO_NAME
				
				if [ ! -d "$GITPATH" ]; then
					echo "Cloning $REPO_TYPE repository '${REPO_NAME}' ..."
					git clone $REPO_URL $GITPATH
					echo -e "Cloning $REPO_TYPE repository '$REPO_NAME' - completed"
				fi
				if [ "$COMMIT_ID" != "" ]; then
					echo "Reseting $REPO_TYPE sources to commit '$COMMIT_ID'"
					cd $REPO_SOURCES_DIR
					git reset --hard $COMMIT_ID
					cd -
				else
					echo "Reseting $REPO_TYPE sources to head"
					cd $REPO_SOURCES_DIR
					git reset --hard HEAD
					cd -
				fi
			else
				echo "Unknown RepoType: $REPO_TYPE"
				exit 1
			fi

			echo "Running Checkstyle on $SITE_SOURCES_DIR ... with excludes $EXCLUDES_ACCUM"
			echo "mvn -e --batch-mode clean site -Dcheckstyle.excludes=$EXCLUDES -DMAVEN_OPTS=-Xmx3024m"
			mvn -e --batch-mode clean site -Dcheckstyle.excludes=$EXCLUDES -DMAVEN_OPTS=-Xmx3024m

			if [ "$?" != "0" ]
			then
				echo "Checkstyle failed on $SITE_SOURCES_DIR"
				exit 1
			else
				echo "Running Checkstyle on $SITE_SOURCES_DIR - finished"
			fi

			echo "linking report to index.html"
			mv target/site/index.html target/site/_index.html
			ln -s checkstyle.html target/site/index.html 

			if $MINIMIZE ; then
			echo "Removing non refernced xref files in report ..."

			# to be safe on removal switch folder to "xref"
			cd target/site/xref

			grep xref ../index.html | grep -v "xref/index.html" |  sed 's/<td><a href=".\/xref\//.\//' | sed -E 's/\.html#L.*/.html'/ | sort | uniq > file.txt
			# such files are required by https://github.com/attatrol/ahsm tool
			echo "allclasses-frame.html" >> file.txt
			echo "index.html" >> file.txt
			echo "overview-frame.html" >> file.txt
			echo "overview-summary.html" >> file.txt

			echo "Backuping files that are refenced in report ..."
			for f in $(cat file.txt) ; do
				if [ -f "$f" ]
				then
					mv "$f" "$f.save"
				else
					echo "warning: $f not found."
				fi
			done

			echo "Removing all non used html files"
			find . -name '*.html' | xargs rm

			echo "Restoring from backup.."
			for f in $(cat file.txt) ; do
				if [ -f "$f.save" ]
				then
					mv "$f.save" "$f"
				else
					echo "warning: $f.save not found."
				fi
			done

			VIOLATIONS=$(grep '<th>Line</th>' ../index.html | wc -l)
			XREF_FILES=$(find . -type f -name "*.html" | wc -l)
			if [[ "$VIOLATIONS" != "0" && "$XREF_FILES" == "0" ]]
			then
				echo "Removing all non used html files, report has violations and xref is empty"
				exit 1
			fi

			# remove all empty folders
			find . -type d -empty -delete
			# return back to original folder
			cd ../../../
			fi

			if [ ! -d "$1" ]; then
				mkdir $1
			fi
			if [ ! -d "$SITE_SAVE_REF_DIR" ]; then
				mkdir $SITE_SAVE_REF_DIR
			fi
			
			# change xml paths to save directory
			sed -i -e "s#$TESTER_DIR/$SITE_SOURCES_DIR#$TESTER_DIR/$SITE_SAVE_REF_DIR/$REPO_NAME#g" target/checkstyle-result.xml
			# save files
			mv target/site $1/$REPO_NAME
			mv target/*.xml $1/$REPO_NAME

			if ! containsElement "$REPO_NAME" "${EXTPROJECTS[@]}" ; then
				EXTPROJECTS+=($REPO_NAME)

				if [ ! -d "$TESTER_DIR/$SITE_SAVE_REF_DIR/$REPO_NAME" ]; then
					mkdir $TESTER_DIR/$SITE_SAVE_REF_DIR/$REPO_NAME
				fi
				rm -rf $TESTER_DIR/$SITE_SAVE_REF_DIR/$REPO_NAME/*
				mv $SITE_SOURCES_DIR/* $TESTER_DIR/$SITE_SAVE_REF_DIR/$REPO_NAME
			fi

			echo "Running Launch on $REPO_NAME - completed"
		done < projects-to-test-on.properties
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

echo "Testing Checkstyle Starting"

if $INSTALL_MASTER ; then
	cd $CHECKSTYLE_DIR

	if $CONTACTSERVER ; then
		git fetch origin
	fi
	git reset --hard HEAD
	git checkout master
	git pull

	echo "Installing Master"

	mvn_install
else
	echo "Skipping Install Master"
fi

if $RUN_MASTER ; then
	echo "Starting Master Launcher"

	cd $TESTER_DIR

	rm -rf $SITE_SAVE_MASTER_DIR
	rm -rf $SITE_SAVE_REF_DIR

	launch $SITE_SAVE_MASTER_DIR
else
	echo "Skipping Launch Master"
fi

if $INSTALL_PULL ; then
	cd $CHECKSTYLE_DIR

	echo "Checking out and Installing PR $1"

	if $CONTACTSERVER ; then
		git fetch $PULL_REMOTE
	fi

	if [ ! `git rev-parse --verify $PULL_REMOTE/$1` ] ;
	then
		echo "Branch $PULL_REMOTE/$1 doesn't exist"
		exit 1
	fi

	git checkout $PULL_REMOTE/$1

	mvn_install
else
	echo "Skipping Install PR $1"
fi

if $RUN_PULL ; then
	echo "Starting PR $1 Launcher"

	cd $TESTER_DIR

	rm -rf $SITE_SAVE_PULL_DIR

	launch $SITE_SAVE_PULL_DIR
else
	echo "Skipping Launch PR $1"
fi

if ! $RUN_MASTER && ! $RUN_PULL ; then
	echo "Figuring out Reports to run"

	while read line ; do
		[[ "$line" == \#* ]] && continue # Skip lines with comments
		[[ -z "$line" ]] && continue     # Skip empty lines
		
		REPO_NAME=`echo $line | cut -d '|' -f 1`

		EXTPROJECTS+=($REPO_NAME)
	done < $TESTER_DIR/projects-to-test-on.properties
fi

echo "Starting all Reports"

if [ ! -d "$FINAL_RESULTS_DIR" ]; then
	mkdir $FINAL_RESULTS_DIR
else
	rm -rf $FINAL_RESULTS_DIR/*
fi

if [ -f $FINAL_RESULTS_DIR/index.html ] ; then
	rm $FINAL_RESULTS_DIR/index.html
fi
echo "<html><body>" >> $FINAL_RESULTS_DIR/index.html

for extp in "${EXTPROJECTS[@]}"
do
	if [ ! -d "$FINAL_RESULTS_DIR/$extp" ]; then
		echo "java -jar $DIFF_JAR --baseReport $TESTER_DIR/$SITE_SAVE_MASTER_DIR/$extp/checkstyle-result.xml --patchReport $TESTER_DIR/$SITE_SAVE_PULL_DIR/$extp/checkstyle-result.xml --output $FINAL_RESULTS_DIR/$extp --refFiles $TESTER_DIR"

		java -jar $DIFF_JAR --baseReport $TESTER_DIR/$SITE_SAVE_MASTER_DIR/$extp/checkstyle-result.xml --patchReport $TESTER_DIR/$SITE_SAVE_PULL_DIR/$extp/checkstyle-result.xml --output $FINAL_RESULTS_DIR/$extp --refFiles $TESTER_DIR
		
		if [ "$?" != "0" ]
		then
			echo "patch-diff-report-tool failed on $extp"
			exit 1
		fi
	else
		echo "Skipping patch-diff-report-tool for $extp"
	fi

	total=($(grep -Eo 'totalDiff">[0-9]+' $FINAL_RESULTS_DIR/$extp/index.html | grep -Eo '[0-9]+'))

	echo "<a href='$extp/index.html'>$extp</a>" >> $FINAL_RESULTS_DIR/index.html
	if [ ${#total[@]} != "0" ] ; then
		if [ ${total[0]} -ne 0 ] ; then
			echo " (${total[0]})" >> $FINAL_RESULTS_DIR/index.html
		fi
	fi
	echo "<br />" >> $FINAL_RESULTS_DIR/index.html
done

echo "</body></html>" >> $FINAL_RESULTS_DIR/index.html
echo "Complete"

exit 0