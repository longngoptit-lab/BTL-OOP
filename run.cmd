@echo off
setlocal
cd /d "%~dp0"
call mvnw.cmd -q -DskipTests package
if errorlevel 1 exit /b 1
java -jar target\qlht.jar %*
exit /b %ERRORLEVEL%
