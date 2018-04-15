#!/bin/bash

set -e

MAX_CONTAINER_TTL_SECONDS=${CONTAINER_TTL_SECONDS:-10} # 1800 # 30 minutes
MAX_IMAGE_TTL_SECONDS=${IMAGE_TTL_SECONDS:-86400} # 1 day

CURRENT_DATE_EPOCH=`date +%s`

function to_human_readable_time {
  local T=$1
  local D=$((T/60/60/24))
  local H=$((T/60/60%24))
  local M=$((T/60%60))
  local S=$((T%60))
  (( $D > 0 )) && printf '%d days ' $D
  (( $H > 0 )) && printf '%d hours ' $H
  (( $M > 0 )) && printf '%d minutes ' $M
  (( $D > 0 || $H > 0 || $M > 0 )) && printf 'and '
  printf '%d seconds\n' $S
}

function remove_too_old_containers {

  HYPER_CONTAINERS=`hyper ps -aq`

  for HYPER_CONTAINER in $HYPER_CONTAINERS; do
    CONTAINER_INFO=`hyper inspect -f '{{.ID}} {{.State.Running}} {{.State.StartedAt}} {{.Name}} {{.Config.Image}}' "$HYPER_CONTAINER"`
    CONTAINER_ID=`echo "$CONTAINER_INFO" | awk '{print $1}' | cut -c -6`
    CONTAINER_IS_RUNNING=`echo "$CONTAINER_INFO" | awk '{print $2}'`  
    CONTAINER_LAUNCHED_AT=`echo "$CONTAINER_INFO" | awk '{print $3}'`
    CONTAINER_NAME=`echo "$CONTAINER_INFO" | awk '{print $4}'`
    CONTAINER_IMAGE=`echo "$CONTAINER_INFO" | awk '{print $5}'`
    CONTAINER_LAUNCHED_AT_EPOCH=`date -d $CONTAINER_LAUNCHED_AT +%s`

    CONTAINER_EXISTSENCE_TIME_SECONDS=$((CURRENT_DATE_EPOCH-CONTAINER_LAUNCHED_AT_EPOCH))

    if [[ "$CONTAINER_EXISTSENCE_TIME_SECONDS" -gt "$MAX_CONTAINER_TTL_SECONDS" ]]; then
      echo "Removing container [$CONTAINER_NAME $CONTAINER_ID $CONTAINER_IMAGE RUNNING=$CONTAINER_IS_RUNNING] that is running for $(to_human_readable_time $CONTAINER_EXISTSENCE_TIME_SECONDS) ..."
      hyper rm -f "$CONTAINER_ID" > /dev/null
    fi
  done
}

function remove_old_not_whitelisted_images {

  HYPER_IMAGES=`hyper images -aq`

  for HYPER_IMAGE in $HYPER_IMAGES; do
    IMAGE_CREATED_DATE=`hyper inspect -f '{{.Created}}' $HYPER_IMAGE`
    IMAGE_CREATED_DATE_EPOCH=`date -d $IMAGE_CREATED_DATE +%s`

    IMAGE_EXISTSENCE_TIME_SECONDS=$((CURRENT_DATE_EPOCH-IMAGE_CREATED_DATE_EPOCH))
    if [[ "$IMAGE_EXISTSENCE_TIME_SECONDS" -gt "$MAX_IMAGE_TTL_SECONDS" ]]; then
      echo "Removing image [$HYPER_IMAGE] that exists for $(to_human_readable_time $IMAGE_EXISTSENCE_TIME_SECONDS) ..."
      hyper rmi "$HYPER_IMAGE" 2>&1 || true
    fi
  done
}

function main {
  remove_too_old_containers
  remove_old_not_whitelisted_images
}

main
