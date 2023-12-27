#!/bin/bash
echo "Initializing backend application"

# NAVIGATE TO MAIN PROJECT DIR
PROJECT_DIR=$(pwd)
PARENT_DIR=${PROJECT_DIR%/"green-cloud"*}
cd "${PARENT_DIR}/green-cloud" || exit

# INITIALIZE BACKEND APPLICATION
echo "Cleaning previous compilation..."
mvn clean &&

echo "Installing modified JADE dependency..."
mvn install:install-file -Dfile='.\lib\jade.jar' -DgroupId='com.tilab.jade' -DartifactId=jade -Dversion='4.6' -Dpackaging=jar &&

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

echo "Initialization of the application has finished!"

# NAVIGATE BACK TO COMPILE DIRECTORY
cd ./compile || exit
