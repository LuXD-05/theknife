#!/bin/bash
# Avvia il ngrok in un container Docker (richiede Docker installato).
cd "$(dirname "$0")"
docker compose up -d ngrok
echo ""
echo "Container ngrok avviato!"
