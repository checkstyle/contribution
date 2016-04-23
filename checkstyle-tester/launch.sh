#!/bin/bash
set -e

SOURCES_DIR=src/main/java

echo "Testing Checkstyle started"

export EXCLUDES_ACCUM=""

while read line ; do
    [[ "$line" == \#* ]] && continue # Skip lines with comments
    [[ -z "$line" ]] && continue     # Skip empty lines
    
    REPO_NAME=`echo $line | cut -d '|' -f 1`
    REPO_TYPE=`echo $line | cut -d '|' -f 2`
    REPO_URL=` echo $line | cut -d '|' -f 3`
    COMMIT_ID=`echo $line | cut -d '|' -f 4`
    EXCLUDES=` echo $line | cut -d '|' -f 5`
    
    EXCLUDES_ACCUM+=",$EXCLUDES"

    REPO_SOURCES_DIR=$SOURCES_DIR/$REPO_NAME
    
    if [ "$REPO_TYPE" == "github" ]; then
        rm -rf $REPO_SOURCES_DIR
        if [ ! -d "$REPO_SOURCES_DIR" ]; then
            TARNAME=$(echo $REPO_URL | sed -E 's/\//-/')
            TARPATH=$SOURCES_DIR/$TARNAME".tar.gz"
            if [ ! -f "$TARPATH" ]; then
                echo "Requesting a tar: 'wget https://api.github.com/repos/$REPO_URL/tarball/$COMMIT_ID -O $TARPATH'"
                wget https://api.github.com/repos/$REPO_URL/tarball/$COMMIT_ID -O $TARPATH
                fi

            tar -xf $TARPATH -C $SOURCES_DIR
            UNTARRED_DIR=`find $SOURCES_DIR -maxdepth 1 -type d -name "$TARNAME-*"| head -n1`
            mv $UNTARRED_DIR $REPO_SOURCES_DIR

            echo -e "untar $TARNAME file is done to $REPO_SOURCES_DIR - completed\n"
        fi
    elif [ "$REPO_TYPE" == "git" ]; then
		if [ ! -d "$REPO_SOURCES_DIR" ]; then
			echo "Cloning $REPO_TYPE repository '${REPO_NAME}' to $REPO_SOURCES_DIR folder ..."
			git clone $REPO_URL $REPO_SOURCES_DIR
			echo -e "Cloning $REPO_TYPE repository '$REPO_NAME' - completed\n"
			fi

		if [ "$COMMIT_ID" != "" ]; then
			echo "Reseting $REPO_TYPE sources to commit '$COMMIT_ID'"
			cd $REPO_SOURCES_DIR
			git reset --hard $COMMIT_ID
			cd -
			fi
    else
        # esle is only hg (mercurial)
		if [ ! -d "$REPO_SOURCES_DIR" ]; then
			echo "Cloning $REPO_TYPE repository '${REPO_NAME}' to $REPO_SOURCES_DIR folder ..."
			hg clone $REPO_URL $REPO_SOURCES_DIR
			echo -e "Cloning $REPO_TYPE repository '$REPO_NAME' - completed\n"
			fi

		if [ "$COMMIT_ID" != "" ]; then
			echo "Reseting $REPO_TYPE sources to commit '$COMMIT_ID'"
			cd $REPO_SOURCES_DIR
			hg up $COMMIT_ID
			cd -
			fi 
    
    fi
    
    echo -e "$REPO_NAME is synchronized\n"

done < projects-to-test-on.properties

echo "Running 'mvn clean' on $SOURCES_DIR ..."
mvn --batch-mode clean

# If you run into OutOfMemoryError please use MAVEN_OPTS
#export MAVEN_OPTS="-Xmx3000m"

echo "Running Checkstyle on $SOURCES_DIR ... with excludes $EXCLUDES_ACCUM"
if [ "$EXCLUDES" == "" ]; then
	mvn -e --batch-mode site -Dcheckstyle.excludes=$EXCLUDES_ACCUM "$@"
else
	mvn -e --batch-mode site -Dcheckstyle.excludes=$EXCLUDES_ACCUM -Djxr-plugin.exclude=$EXCLUDES "$@"
fi
if [ "$?" != "0" ]
then
	echo "Checkstyle is failed on $SOURCES_DIR"
	exit 1
else
    echo "Running Checkstyle on $SOURCES_DIR - finished"
fi


echo "linking report to index.html"
mv target/site/index.html target/site/_index.html
ln -s checkstyle.html target/site/index.html 

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

echo "Done. Result report is locates at: target/site/index.html"
