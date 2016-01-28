#!/bin/bash
set -e

# ============================================================
# Custom Options
# Note: Use full paths
# ============================================================

MINIMIZE=false
CONTACTSERVER=false

CHECKSTYLE_DIR=~/opensource/checkstyle
TESTER_DIR=~/opensource/contribution/checkstyle-tester
EXTRA_DIR=~/opensource/downloads
FINAL_RESULTS_DIR=~/opensource/results
AHSM_JAR=~/Downloads/ahsm.jar

# Note: Full paths no longer needed

PULL_REMOTE=pull

SITE_SOURCES_DIR_NAME=src/main/java
SITE_SAVE_MASTER_DIR_NAME=savemaster
SITE_SAVE_PULL_DIR_NAME=savepull

# ============================================================
# ============================================================
# ============================================================

#declare -a EXTPROJECTS
EXTPROJECTS=()

function mvn_install {
	mvn --batch-mode clean install -Dmaven.test.skip=true -Dcheckstyle.ant.skip=true

	if [ $? -ne 0 ]; then
		echo "Maven Install Failed!"
		exit 1
	fi
}

function launch {
		cd $TESTER_DIR

		while read line ; do
			rm -rf $SITE_SOURCES_DIR_NAME/*
			
			[[ "$line" == \#* ]] && continue # Skip lines with comments
			[[ -z "$line" ]] && continue     # Skip empty lines
			
			REPO_NAME=`echo $line | cut -d '|' -f 1`
			REPO_TYPE=`echo $line | cut -d '|' -f 2`
			REPO_URL=` echo $line | cut -d '|' -f 3`
			COMMIT_ID=`echo $line | cut -d '|' -f 4`
			EXCLUDES=` echo $line | cut -d '|' -f 5`
			
			echo "Running Launch on $REPO_NAME ..."
			
			REPO_SOURCES_DIR=$SITE_SOURCES_DIR_NAME/$REPO_NAME
			
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
					tar -xf $TARPATH -C $SITE_SOURCES_DIR_NAME
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

			echo "Running Checkstyle on $SITE_SOURCES_DIR_NAME ... with excludes $EXCLUDES_ACCUM"
			mvn -e --batch-mode clean site -Dcheckstyle.excludes=$EXCLUDES -DMAVEN_OPTS=-Xmx3024m
			if [ "$?" != "0" ]
			then
				echo "Checkstyle failed on $SITE_SOURCES_DIR_NAME"
				exit 1
			else
				echo "Running Checkstyle on $SITE_SOURCES_DIR_NAME - finished"
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
			  mv "$f" "$f.save"
			done

			echo "Removing all non used html files"
			find . -name '*.html' | xargs rm

			echo "Restoring from backup.."
			for f in $(cat file.txt) ; do
			  mv "$f.save" "$f"
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
			
			mv target/site $1/$REPO_NAME
			EXTPROJECTS+=($REPO_NAME)

			echo "Running Launch on $REPO_NAME - completed"
		done < projects-to-test-on.properties
}

# ============================================================
# ============================================================
# ============================================================

echo "Testing Checkstyle Starting"

cd $CHECKSTYLE_DIR

if $CONTACTSERVER ; then
	git fetch origin
	git fetch $PULL_REMOTE
fi
git reset --hard HEAD
git checkout master

echo "Installing Master"

mvn_install

echo "Starting Master Launcher"

cd $TESTER_DIR

rm -rf $SITE_SAVE_MASTER_DIR_NAME
rm -rf $SITE_SAVE_PULL_DIR_NAME

launch $SITE_SAVE_MASTER_DIR_NAME

cd $CHECKSTYLE_DIR

echo "Checking out and Installing PR $1"

git checkout $1

mvn_install

echo "Starting PR $1 Launcher"

launch $SITE_SAVE_PULL_DIR_NAME

echo "Starting all AHSMs"

if [ ! -d "$FINAL_RESULTS_DIR" ]; then
	mkdir $FINAL_RESULTS_DIR
fi

for extp in "${EXTPROJECTS[@]}"
do
	if [ ! -d "$FINAL_RESULTS_DIR/$extp" ]; then
		echo "java -jar $AHSM_JAR checkstyle $TESTER_DIR/$SITE_SAVE_MASTER_DIR_NAME/$extp $TESTER_DIR/$SITE_SAVE_PULL_DIR_NAME/$extp $FINAL_RESULTS_DIR/$extp"
	
		java -jar $AHSM_JAR checkstyle $TESTER_DIR/$SITE_SAVE_MASTER_DIR_NAME/$extp $TESTER_DIR/$SITE_SAVE_PULL_DIR_NAME/$extp $FINAL_RESULTS_DIR/$extp
		
		if [ "$?" != "0" ]
		then
			echo "AHSM failed on $extp"
			exit 1
		fi
	else
		echo "Skipping AHSM for $extp"
	fi
done

echo "Complete"

exit 0