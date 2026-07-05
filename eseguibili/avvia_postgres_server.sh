#!/bin/bash
cd "$(dirname "$0")/.."

echo "Avvio postgres..."
docker compose -f theknife-backend/docker-compose.yml up -d postgres

echo "Attendo che postgres sia pronto..."
sleep 5

echo "Avvio server..."
mvn -pl theknife-backend -am io.quarkus.platform:quarkus-maven-plugin:3.20.0:dev