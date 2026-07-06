@echo off
REM Avvia il client JavaFX TheKnife (Windows). Richiede JDK 21.
REM Si puo' passare l'URL del server come argomento, es:
REM   avvia_client.bat ws://localhost:8080
REM Se omesso, il client chiedera' l'indirizzo in una finestra all'avvio.
cd /d "%~dp0"
java -jar theknife-frontend-win.jar %*
pause
