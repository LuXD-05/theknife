@echo off
REM Avvia il ngrok in un container Docker (richiede Docker installato).
cd /d "%~dp0"
docker compose up -d ngrok
echo.
echo Container ngrok avviato!
pause
