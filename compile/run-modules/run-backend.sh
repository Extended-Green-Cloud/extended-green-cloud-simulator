#!/bin/bash
echo "Running engine..."

trap navigate SIGINT SIGTERM

# NAVIGATE BACK TO COMPILE DIRECTORY IN CASE OF SIGTERM/SIGINT
function navigate() {
  cd ../../compile || exit
}

# NAVIGATE TO ENGINE DIR
PROJECT_DIR=$(pwd)
PARENT_DIR=${PROJECT_DIR%/"green-cloud"*}
cd "${PARENT_DIR}/green-cloud/engine/strategy" || exit

PACKAGE_NAME="green-cloud-engine.jar"

if [ ! -z "$2" ]
then
  PACKAGE_NAME=$2
fi

if [ "$1" == "SINGLE" ]
then
  java -cp "${PACKAGE_NAME}" runner.EngineRunner
elif [ "$1" == "MULTI" ]
then
  java -cp "${PACKAGE_NAME}" runner.MultiEngineRunner
fi

# NAVIGATE BACK TO COMPILE DIRECTORY
navigate
