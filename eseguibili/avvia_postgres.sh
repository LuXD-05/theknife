#!/bin/bash
cd "$(dirname "$0")/.."
docker compose -f theknife-backend/docker-compose.yml up -d postgres
echo ""
echo "Container postgres avviato!"