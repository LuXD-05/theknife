@echo off
cd /d "%~dp0.."
docker compose -f theknife-backend/docker-compose.yml up -d postgres
echo.
echo Container postgres avviato!
pause