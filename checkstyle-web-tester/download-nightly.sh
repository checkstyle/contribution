#!/bin/bash

CS_DIR="/tmp/checkstyle-nightly"

#############################################

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DATE=`date +%Y-%m-%d`
FILE="checkstyle-nightly-$DATE-all.jar"

#############################################

if [ ! -d $CS_DIR ]; then
	mkdir $CS_DIR
fi

cd $CS_DIR

if [ ! -d "$CS_DIR/checkstyle/.git" ]; then
	git clone https://github.com/checkstyle/checkstyle.git

	cd checkstyle
else
	cd checkstyle

	git checkout master
	git fetch origin

	if [ $(git rev-parse HEAD) == $(git rev-parse @{u}) ]; then
		echo "No changes in checkstyle since last run."
		exit 1
	fi

	git reset --hard HEAD
	git pull
fi

mvn --batch-mode clean package -Passembly -Dmaven.test.skip=true -Dcheckstyle.ant.skip=true -Dcheckstyle.skip=true -Dpmd.skip=true -Dspotbugs.skip=true -Djacoco.skip=true -Dforbiddenapis.skip=true

sudo -u www-data cp target/checkstyle-*-SNAPSHOT-all.jar $DIR/$FILE
