#!/bin/bash
# Attention, there is no "-x" to avoid problems on Travis
set -e

case $1 in

releasenotes-builder)
  cd releasenotes-builder
  mvn clean verify
  mvn clean compile package
  ;;

patch-diff-report-tool)
  cd patch-diff-report-tool
  mvn clean install
  ;;

checkstyle-tester-launch-groovy)
  git clone https://github.com/checkstyle/checkstyle
  cd checkstyle
  mvn --batch-mode clean install -Passembly
  cd ../checkstyle-tester
  groovy launch.groovy -l projects-for-travis.properties -c my_check.xml -i
  ;;

checkstyle-tester-diff-groovy-patch)
  git clone https://github.com/romani/checkstyle
  cd checkstyle
  git checkout -b patch-branch
  cd ../checkstyle-tester
  groovy diff.groovy -l projects-for-travis.properties \
    -c my_check.xml -b master -p patch-branch -r ../checkstyle
  ;;

checkstyle-tester-diff-groovy-base-patch)
  git clone https://github.com/romani/checkstyle
  cd checkstyle
  git checkout -b patch-branch
  cd ../checkstyle-tester
  groovy diff.groovy -l projects-for-travis.properties \
    -bc my_check.xml -pc my_check.xml -b master -p patch-branch -r ../checkstyle
  ;;

checkstyle-tester-diff-groovy-patch-only)
  git clone https://github.com/romani/checkstyle
  cd checkstyle
  git checkout -b patch-branch
  cd ../checkstyle-tester
  groovy diff.groovy -l projects-for-travis.properties \
    -pc my_check.xml -p patch-branch -r ../checkstyle -m single
  ;;

codenarc)
  cd checkstyle-tester
  ./codenarc.sh . diff.groovy > diff.log && cat diff.log && grep '(p1=0; p2=0; p3=0)' diff.log
  ./codenarc.sh . launch.groovy > launch.log && cat launch.log && grep '(p1=0; p2=11; p3=1)' launch.log
  ;;

*)
  echo "Unexpected argument: $1"
  sleep 5s
  false
  ;;

esac
