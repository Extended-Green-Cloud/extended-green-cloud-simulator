#!/bin/bash
echo "Initializing data clustering module"

# NAVIGATE TO MAIN PROJECT DIR
PROJECT_DIR=$(pwd)
PARENT_DIR=${PROJECT_DIR%/"green-cloud"*}
cd "${PARENT_DIR}/green-cloud" || exit

# COPY CONFIGURATION FILE
echo "Copying configuration .env file..."
cp -fr ./config/data-clustering/.env ./data-clustering/.env

# NAVIGATE TO MODULES
cd ./data-clustering || exit &&

# CLEAR PREVIOUS NODE MODULES (IF THEY EXIST)
echo "Installing requirements..."
pip install -r requirements.txt

echo "Initialization of data clustering module completed!"

# NAVIGATE BACK TO COMPILE DIRECTORY
cd ../compile || exit
