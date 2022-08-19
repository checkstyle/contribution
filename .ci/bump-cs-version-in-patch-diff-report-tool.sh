#!/bin/bash
set -e

cd patch-diff-report-tool
mvn versions:set-property -Dproperty=checkstyle.version -DnewVersion="$1"
echo "Version updated to $1"
