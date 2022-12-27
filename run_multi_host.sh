#!/bin/bash
export MAIN_CLASS=runner.MultiEngineRunner
docker-compose --log-level ERROR -f compose-gc.yml up -d
sleep 5
command -v python3 >/dev/null 2>&1 && python3 -m webbrowser -t "http://localhost:3000"
