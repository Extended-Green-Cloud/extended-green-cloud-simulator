#!/bin/bash
docker compose -f compose-server.yml up -d
npm start --prefix green-cloud-ui &
sleep 5
docker compose -f compose-engine.yml up -d
