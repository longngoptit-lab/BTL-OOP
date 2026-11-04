@echo off
cd /d "%~dp0"
java -jar qlht.jar %*
if errorlevel 1 pause
