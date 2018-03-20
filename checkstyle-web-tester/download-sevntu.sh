#!/bin/bash

CS_DIR="/tmp/checkstyle-nightly"
SEVNTU_DIR="/tmp/sevntu-nightly"

#############################################

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DATE=`date +%Y-%m-%d`
FILE="checkstyle-$1-sevntu-$2-all.jar"

#############################################

if [ "$#" -ne 2 ]; then
	echo "Expecting only 2 arguments, checkstyle version and sevntu version"
	exit 1
fi

if [ ! -d $CS_DIR ]; then
	mkdir $CS_DIR
fi

cd $CS_DIR

if [ ! -d "$CS_DIR/checkstyle/.git" ]; then
	git clone https://github.com/checkstyle/checkstyle.git

	cd checkstyle
else
	cd checkstyle

	git fetch origin

	git reset --hard HEAD
	git clean -f -d
fi

git checkout tags/checkstyle-$1

#############################################

if [ ! -d $SEVNTU_DIR ]; then
	mkdir $SEVNTU_DIR
fi

cd $SEVNTU_DIR

if [ ! -d "$SEVNTU_DIR/sevntu.checkstyle/.git" ]; then
	git clone https://github.com/sevntu-checkstyle/sevntu.checkstyle.git

	cd sevntu.checkstyle
else
	cd sevntu.checkstyle

	git fetch origin

	if [ $(git rev-parse HEAD) == $(git rev-parse @{u}) ]; then
		echo "No changes in sevntu.checkstyle since last run."
		exit 1
	fi

	git reset --hard HEAD
	git clean -f -d
fi

git checkout tags/v$2

cp -r sevntu-checks/src/main/java $CS_DIR/checkstyle/src/main
rm sevntu-checks/src/main/resources/checkstyle_packages.xml
cp -r sevntu-checks/src/main/resources $CS_DIR/checkstyle/src/main

cd $CS_DIR/checkstyle

#############################################

mvn --batch-mode clean package -Passembly -Dmaven.test.skip=true -Dcheckstyle.ant.skip=true -Dcheckstyle.skip=true -Dpmd.skip=true -Dspotbugs.skip=true -Djacoco.skip=true -Dforbiddenapis.skip=true

sudo -u www-data cp target/checkstyle-*-all.jar $DIR/$FILE
