#!/bin/bash

# Shows the labels.
function showLabel() {
  if [[ "$0" == *"build.sh"* ]]; then
    echo "** Build **"
  elif [[ "$0" == *"package.sh"* ]]; then
      echo "** Package **"
  elif [[ "$0" == *"publish.sh"* ]]; then
    echo "** Publish **"
  elif [[ "$0" == *"stop.sh"* ]]; then
      echo "** Stop **"
  elif [[ "$0" == *"start.sh"* ]]; then
      echo "** Start **"
  fi

  echo
}

# Shows the banner.
function showBanner() {
  if [ -f "banner.txt" ]; then
    cat banner.txt
  fi

  showLabel
}

# Prepares the environment to execute the commands of this script.
function prepareToExecute() {
  # Required files/paths.
  export WORK_DIR="$PWD"
  export BUILD_ENV_FILENAME="$WORK_DIR/.env"

  # Required binaries.
  export JAVA_CMD=$(which java)
  export DOCKER_CMD=$(which docker)

  # Environment variables.
  if [ -f "$BUILD_ENV_FILENAME" ]; then
    source "$BUILD_ENV_FILENAME"
  fi
}

prepareToExecute