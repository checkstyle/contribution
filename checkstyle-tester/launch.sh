#!/bin/bash

SOURCES_DIR=src/main/java

echo "Testing Checkstyle started"

export EXCLUDES_ACCUM=""

while read line ; do
    [[ "$line" == \#* ]] && continue # Skip lines with comments
    [[ -z "$line" ]] && continue     # Skip empty lines
    
    REPO_NAME=`echo $line | cut -d '|' -f 1`
    REPO_URL=` echo $line | cut -d '|' -f 2`
    COMMIT_ID=`echo $line | cut -d '|' -f 3`
    EXCLUDES=`echo $line | cut -d '|' -f 4`
    
    EXCLUDES_ACCUM+=",$EXCLUDES"

    REPO_SOURCES_DIR=$SOURCES_DIR/$REPO_NAME
    
    if [ ! -d "$REPO_SOURCES_DIR" ]; then
        echo "Cloning repository '${REPO_NAME}' ..."
        git clone $REPO_URL $REPO_SOURCES_DIR
        echo -e "Cloning repository '$REPO_NAME' - completed\n"
    fi

	if [ "$COMMIT_ID" != "" ]; then
        echo "Reseting sources to commit '$COMMIT_ID'"
        cd $REPO_SOURCES_DIR
	    git reset --hard $COMMIT_ID
        cd -
	    fi
    echo -e "$REPO_NAME is synchronized\n"

done < projects-to-test-on.properties

echo "Running 'mvn clean' on $SOURCES_DIR ..."
mvn --batch-mode clean

# If you run into OutOfMemoryError please use MAVEN_OPTS
#export MAVEN_OPTS="-Xmx3000m"

echo "Running Checkstyle on $SOURCES_DIR ... with excludes $EXCLUDES_ACCUM"
mvn --batch-mode site -Dcheckstyle.excludes=$EXCLUDES_ACCUM "$@"
if [ "$?" != "0" ]
then
	echo "Checkstyle is failed on $SOURCES_DIR"
	exit 1
else
    echo "Running Checkstyle on $SOURCES_DIR - finished"
fi


echo "linking report to index.html"
mv target/site/index.html target/site/index_.html
ln -s checkstyle.html target/site/index.html 

echo "Done."
