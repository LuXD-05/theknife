@echo off
REM Avvia il database PostgreSQL in un container Docker (richiede Docker installato).
cd /d "%~dp0"
docker compose up -d postgres
echo.
echo Container postgres avviato!
pause
