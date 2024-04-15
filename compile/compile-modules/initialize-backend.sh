#!/bin/bash
echo "Initializing backend application"

# NAVIGATE TO MAIN PROJECT DIR
PROJECT_NAME="green-cloud"
PROJECT_DIR=$(pwd)
PARENT_DIR=${PROJECT_DIR%/${PROJECT_NAME}*}
cd "${PARENT_DIR}/${PROJECT_NAME}" || exit

# INITIALIZE BACKEND APPLICATION
echo "Cleaning previous compilation..."
mvn clean &&

echo "Initializing database..."
(source ./knowledge-database/run_database.sh || echo "Database could not be initialized") &&

echo "Running database..."
(source ./compile/run-modules/run-database.sh || echo "Database could not be started") &&

echo "Compiling classes..."
mvn compile &&

echo "Building packages..."
mvn package &&

# COPY CONFIGURATION FILES TO RUNNABLE FOLDER
cp -R ./engine/src/main/resources/scenarios/. ./engine/runnable/scenarios/
cp -R ./engine/src/main/resources/properties/. ./engine/runnable/properties/
cp -R ./engine/src/main/resources/knowledge/. ./engine/runnable/knowledge/
cp -R ./engine/src/main/resources/samples/. ./engine/runnable/samples/
cp -R ./engine/src/main/resources/strategy/. ./engine/runnable/strategy/

echo "Initialization of the application has finished!"

# NAVIGATE BACK TO COMPILE DIRECTORY
cd ./compile || exit
