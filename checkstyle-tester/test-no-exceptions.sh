#!/usr/bin/env bash

VERSION=6.10-SNAPSHOT

#CHECKSTYLE_REPO=/home/rivanov/checkstyle-test/checkstyle
CHECKSTYLE_REPO=checkstyle
JAR_ALL=$CHECKSTYLE_REPO/target/checkstyle-$VERSION-all.jar

cd /var/tmp/
rm -rf checkstyle
rm -rf guava-libraries
rm -rf jdk

git clone https://github.com/checkstyle/checkstyle.git

#compile
cd checkstyle
mvn clean install -Passembly
cd ../

if [ ! -f $JAR_ALL ]; then
    echo "File $JAR_ALL not found!"
    exit 1;
fi

# clone testing repos
git clone https://code.google.com/p/guava-libraries/
hg clone http://hg.openjdk.java.net/jdk7/jdk7/jdk/


#run validation
java -jar $JAR_ALL \
     -c /google_checks.xml  \
     -o checkstyle-report-checkstyle.txt checkstyle

java -jar $JAR_ALL \
    -c /google_checks.xml  \
    -o checkstyle-report-guava.txt guava-libraries

java -jar $JAR_ALL \
    -c /sun_checks.xml  \
    -o checkstyle-report-jdk.txt jdk/src/share/classes

echo "Grep for Exception:"

# "Got an exception" is from checkstyle/src/main/resources/com/puppycrawl/tools/checkstyle/messages.properties
grep "Got an exception" checkstyle-report-guava.txt
grep "Got an exception" checkstyle-report-checkstyle.txt
grep "Got an exception" checkstyle-report-jdk.txt

echo "Done."
