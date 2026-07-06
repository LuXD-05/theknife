#!/bin/bash
# Avvia il database PostgreSQL in un container Docker (richiede Docker installato).
cd "$(dirname "$0")"
docker compose up -d postgres
echo ""
echo "Container postgres avviato!"
