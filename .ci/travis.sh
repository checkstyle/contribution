#!/bin/bash
# Attention, there is no "-x" to avoid problems on Travis
set -e

case $1 in

some-goal)
  ;;

*)
  echo "Unexpected argument: $1"
  sleep 5s
  false
  ;;

esac
