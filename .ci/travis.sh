#!/bin/bash
# Attention, there is no "-x" to avoid problems on Travis
set -e

function checkout_from {
  CLONE_URL=$1
  PROJECT=$(echo "$CLONE_URL" | sed -nE 's/.*\/(.*).git/\1/p')
  mkdir -p .ci-temp
  cd .ci-temp
  if [ -d "$PROJECT" ]; then
    echo "Target project $PROJECT is already cloned, latest changes will be fetched"
    cd $PROJECT
    git fetch
    cd ../
  else
    for i in 1 2 3 4 5; do git clone $CLONE_URL && break || sleep 15; done
  fi
  cd ../
}

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
  checkout_from https://github.com/checkstyle/checkstyle
  cd .ci-temp/checkstyle
  mvn --batch-mode clean install -Passembly
  cd ../../checkstyle-tester
  groovy launch.groovy -l projects-for-travis.properties -c my_check.xml -i
  ;;

checkstyle-tester-diff-groovy-patch)
  checkout_from https://github.com/checkstyle/checkstyle
  cd .ci-temp/checkstyle
  git checkout -b patch-branch
  cd ../../checkstyle-tester
  sed -i'' 's/^guava/#guava/' projects-for-wercker.properties
  sed -i'' 's/#checkstyle/checkstyle/' projects-for-wercker.properties
  groovy diff.groovy -l projects-for-travis.properties \
    -c my_check.xml -b master -p patch-branch -r ../.ci-temp/checkstyle
  ;;

checkstyle-tester-diff-groovy-base-patch)
  checkout_from https://github.com/checkstyle/checkstyle
  cd .ci-temp/checkstyle
  git checkout -b patch-branch
  cd ../../checkstyle-tester
  groovy diff.groovy -l projects-for-travis.properties \
    -bc my_check.xml -pc my_check.xml -b master -p patch-branch -r ../.ci-temp/checkstyle
  ;;

checkstyle-tester-diff-groovy-patch-only)
  checkout_from https://github.com/checkstyle/checkstyle
  cd .ci-temp/checkstyle
  git checkout -b patch-branch
  cd ../../checkstyle-tester
  groovy diff.groovy -l projects-for-travis.properties \
    -pc my_check.xml -p patch-branch -r ../.ci-temp/checkstyle -m single
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
