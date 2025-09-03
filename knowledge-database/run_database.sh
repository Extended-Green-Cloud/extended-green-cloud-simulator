#!/bin/sh

docker run -d --name timescaledb -p 5432:5432 -e POSTGRES_PASSWORD=password timescale/timescaledb-ha:pg14-latest

docker exec timescaledb bash -c "echo \"max_locks_per_transaction = 128\" >> /var/lib/postgresql/data/postgresql.conf"
docker restart timescaledb
