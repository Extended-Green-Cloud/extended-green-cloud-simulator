#!/bin/bash
echo "Running Data Analysis Server..."

trap navigate SIGINT SIGTERM

# NAVIGATE BACK TO COMPILE DIRECTORY IN CASE OF SIGTERM/SIGINT
function navigate() {
  cd ../compile || exit
}

# NAVIGATE TO ENGINE DIR
PROJECT_DIR=$(pwd)
PARENT_DIR=${PROJECT_DIR%/"green-cloud"*}
cd "${PARENT_DIR}/green-cloud/data-clustering" || exit

# RUN DATA ANALYSIS API SERVER
python server_runner.py

# NAVIGATE BACK TO COMPILE DIRECTORY
navigate
