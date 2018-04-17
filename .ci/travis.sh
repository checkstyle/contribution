#!/bin/bash
# Attention, there is no "-x" to avoid problems on Travis
set -e

case $1 in

jsoref-spellchecker)
  git clone https://github.com/checkstyle/checkstyle && cd checkstyle && mkdir -p .ci-temp/contribution
  cp -r ../jsoref-spellchecker .ci-temp/contribution
  export skipFetchRepo=true && ./.ci/test-spelling-unknown-words.sh
  ;;

*)
  echo "Unexpected argument: $1"
  sleep 5s
  false
  ;;

esac
