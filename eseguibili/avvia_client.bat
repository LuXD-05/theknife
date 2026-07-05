@echo off
cd /d "%~dp0.."
call mvn -pl theknife-common -am install
if errorlevel 1 (
    echo Build di theknife-common fallita!
    pause
    exit /b 1
)
cd theknife-frontend
call mvn org.openjfx:javafx-maven-plugin:0.0.8:run
pause