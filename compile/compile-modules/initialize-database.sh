#!/bin/bash
echo "Initializing database"

# NAVIGATE TO MAIN PROJECT DIR
PROJECT_NAME="green-cloud"
PROJECT_DIR=$(pwd)
PARENT_DIR=${PROJECT_DIR%/${PROJECT_NAME}*}
cd "${PARENT_DIR}/${PROJECT_NAME}" || exit

echo "Initializing database..."
(source ./knowledge-database/run_database.sh || echo "Database could not be initialized") &&

echo "Initialization of the database has finished!"

# NAVIGATE BACK TO COMPILE DIRECTORY
cd ./compile || exit
