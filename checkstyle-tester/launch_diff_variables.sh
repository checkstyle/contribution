#!/bin/bash

# ============================================================
# Custom Options
# Note: Use full paths
# ============================================================

CONTACTSERVER=true

PULL_REMOTE=origin

CHECKSTYLE_DIR=~/src/github.com/pbludov/checkstyle
SEVNTU_DIR=~/src/github.com/pbludov/sevntu.checkstyle
CONTRIBUTION_DIR=~/src/github.com/pbludov/contribution
TEMP_DIR=/tmp/launch_diff

TESTER_DIR=$CONTRIBUTION_DIR/checkstyle-tester
DIFF_JAR=$CONTRIBUTION_DIR/patch-diff-report-tool/target/patch-diff-report-tool-0.1-SNAPSHOT-jar-with-dependencies.jar

REPOSITORIES_DIR=$TESTER_DIR/repositories
FINAL_RESULTS_DIR=$TESTER_DIR/reports/diff

SITE_SAVE_MASTER_DIR=$TESTER_DIR/reports/savemaster
SITE_SAVE_PULL_DIR=$TESTER_DIR/reports/savepull

# to be removed after confirmation
MINIMIZE=true
# to be removed after antlr update
SITE_SOURCES_DIR=$TESTER_DIR/src/main/java
SITE_SAVE_REF_DIR=$TESTER_DIR/reports/saverefs

# ============================================================
# ============================================================
# ============================================================
