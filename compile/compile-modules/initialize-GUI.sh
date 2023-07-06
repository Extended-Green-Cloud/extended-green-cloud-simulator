#!/bin/bash
echo "Initializing GUI"

# NAVIGATE TO MAIN PROJECT DIR
PROJECT_DIR=$(pwd)
PARENT_DIR=${PROJECT_DIR%/"green-cloud"*}
cd "${PARENT_DIR}/green-cloud" || exit

# COPY CONFIGURATION FILE
echo "Copying configuration .env file..."
cp -fr ./config/green-cloud-ui/.env ./green-cloud-ui/.env

# NAVIGATE TO MODULES
cd ./green-cloud-ui || exit &&

# CLEAR PREVIOUS NODE MODULES (IF THEY EXIST)
echo "Cleaning previous node modules..."
npm run clean

# INSTALL NODE MODULES
echo "Installing node modules..."
npm install

echo "Initialization of GUI completed!"

# NAVIGATE BACK TO COMPILE DIRECTORY
cd ../compile || exit
