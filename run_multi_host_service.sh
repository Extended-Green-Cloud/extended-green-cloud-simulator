#!/bin/bash
export MAIN_CLASS=runner.MultiEngineRunner
docker-compose -f compose-gc.yml up -d $SERVICE
