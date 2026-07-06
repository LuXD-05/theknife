@echo off
REM Avvia il backend TheKnife (Quarkus). Richiede JDK 21 e PostgreSQL gia' avviato
REM (vedi avvia_postgres.bat). Al primo avvio, se il database e' vuoto, importa i dati
REM dalla cartella "data" presente in questa directory.
cd /d "%~dp0"
java -jar theknife-backend.jar
pause
