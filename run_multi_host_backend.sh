#!/bin/bash
export MAIN_CLASS=runner.MultiEngineRunner
docker-compose --log-level ERROR -f compose-backend.yml up -d backend