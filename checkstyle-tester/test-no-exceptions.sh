#!/usr/bin/env bash

cd /var/tmp/
rm -rf checkstyle
rm -rf guava-libraries
rm -rf jdk

git clone https://github.com/checkstyle/checkstyle.git

#compile
cd checkstyle

CS_POM_VERSION=$(mvn -q -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)

mvn clean install -Passembly
cd ../

JAR_ALL=checkstyle/target/checkstyle-CS_POM_VERSION-all.jar


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
