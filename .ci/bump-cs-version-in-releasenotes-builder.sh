#!/bin/bash
set -e

cd releasenotes-builder
mvn versions:set-property -Dproperty=checkstyle.version -DnewVersion="$1"
echo "Version updated to $1"
