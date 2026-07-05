@echo off
cd /d "%~dp0.."

echo Avvio postgres...
docker compose -f theknife-backend/docker-compose.yml up -d postgres

echo Attendo che postgres sia pronto...
timeout /t 5 /nobreak >nul

echo Avvio server...
call mvn -pl theknife-backend -am io.quarkus.platform:quarkus-maven-plugin:3.20.0:dev
pause