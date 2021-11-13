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

update-settings-xml)
  MVN_SETTINGS=${TRAVIS_HOME}/.m2/settings.xml
  if [[ -f ${MVN_SETTINGS} ]]; then
    if [[ $TRAVIS_OS_NAME == 'osx' ]]; then
      sed -i'' -e "/<mirrors>/,/<\/mirrors>/ d" $MVN_SETTINGS
    else
      xmlstarlet ed --inplace -d "//mirrors" $MVN_SETTINGS
    fi
  fi
  ;;

releasenotes-builder)
  cd releasenotes-builder
  mvn clean verify
  mvn clean compile package
  ;;

patch-diff-report-tool)
  cd patch-diff-report-tool
  mvn clean install
  ;;

checkstyle-tester-diff-groovy-patch)
  checkout_from https://github.com/checkstyle/checkstyle
  cd .ci-temp/checkstyle
  git checkout -b patch-branch
  cd ../../checkstyle-tester
  cp projects-for-travis.properties ../.ci-temp/projects-for-travis.properties
  sed -i'' 's/^guava/#guava/' ../.ci-temp/projects-for-travis.properties
  sed -i'' 's/#checkstyle/checkstyle/' ../.ci-temp/projects-for-travis.properties
  groovy diff.groovy -l ../.ci-temp/projects-for-travis.properties \
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

checkstyle-tester-diff-groovy-regression-single)
  # Check out lateset checkstyle from master
  rm -rf .ci-temp
  checkout_from https://github.com/checkstyle/checkstyle

  # Run report from master branch of contribution
  checkout_from https://github.com/checkstyle/contribution
  cd .ci-temp/contribution/checkstyle-tester
  sed -i'' 's/^guava/#guava/' projects-to-test-on.properties
  sed -i'' 's/#checkstyle|/checkstyle|/' projects-to-test-on.properties
  export MAVEN_OPTS="-Xmx2048m"
  groovy ./diff.groovy --listOfProjects projects-to-test-on.properties \
    -pc ../../../checkstyle-tester/diff-groovy-regression-config.xml \
    -r ../../checkstyle -xm "-Dcheckstyle.failsOnError=false" \
    -m single -p master

  # Run report with current branch
  cd ../../../checkstyle-tester/
  sed -i'' 's/^guava/#guava/' projects-to-test-on.properties
  sed -i'' 's/#checkstyle|/checkstyle|/' projects-to-test-on.properties
  rm -rf reports repositories
  groovy ./diff.groovy --listOfProjects projects-to-test-on.properties \
    -pc diff-groovy-regression-config.xml -r ../.ci-temp/checkstyle/ \
    -m single -p master -xm "-Dcheckstyle.failsOnError=false"

  cd ..
  # We need to ignore file paths below, since they will be different between reports
  diff -I "contribution" checkstyle-tester/reports/diff/checkstyle/index.html \
    .ci-temp/contribution/checkstyle-tester/reports/diff/checkstyle/index.html
  ;;

codenarc)
  cd checkstyle-tester
  ./codenarc.sh . diff.groovy > diff.log && cat diff.log && grep '(p1=0; p2=13; p3=17)' diff.log
  ;;

markdownlint)
  # The folder "comment-action" is excluded since it contains many 3rd party files that do not pass the validation
  files=$(git ls-files -- '*.md' ':!:comment-action')
  mdl ${files}
  ;;

*)
  echo "Unexpected argument: $1"
  sleep 5s
  false
  ;;

esac
