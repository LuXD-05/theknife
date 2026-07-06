@echo off
cd /d "%~dp0.."
call mvn -pl theknife-backend -am io.quarkus.platform:quarkus-maven-plugin:3.20.0:dev
pause