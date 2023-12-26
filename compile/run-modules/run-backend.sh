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
cd "${PARENT_DIR}/green-cloud/engine/runnable" || exit

PACKAGE_NAME="green-cloud-engine.jar"

if [ ! -z "$2" ]
then
  PACKAGE_NAME=$2
fi

# COPY CONFIGURATION TO JAR

cp -R ../src/main/resources/scenarios/. ./scenarios/
cp -R ../src/main/resources/properties/. ./properties/
cp -R ../src/main/resources/knowledge/. ./knowledge/
cp -R ../src/main/resources/samples/. ./samples/

jar -uvf "${PACKAGE_NAME}" -C properties .
jar -uvf "${PACKAGE_NAME}" -C scenarios .
jar -uvf "${PACKAGE_NAME}" -C knowledge .
jar -uvf "${PACKAGE_NAME}" -C samples .

if [ "$1" == "MULTI" ]
then
  java -cp "${PACKAGE_NAME}" runner.MultiEngineRunner
else
  java -cp "${PACKAGE_NAME}" runner.EngineRunner
fi

# NAVIGATE BACK TO COMPILE DIRECTORY
navigate
