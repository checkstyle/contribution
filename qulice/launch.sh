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
            TARNAME=$(echo $REPO_URL | sed -r 's/\//-/')
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
			echo "Cloning $REPO_TYPE repository '${REPO_NAME}' ..."
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
			echo "Cloning $REPO_TYPE repository '${REPO_NAME}' ..."
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
mvn --batch-mode site -Dcheckstyle.excludes=$EXCLUDES_ACCUM "$@"
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

echo "Done. Result report is locates at: target/site/index.html"
