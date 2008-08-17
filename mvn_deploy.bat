@echo off
call mvn clean deploy && goto :eof
echo.
pause
