@echo off
:: Copyright (c) 2008-2010 Esko Luontola <www.orfjackal.net>
:: This software is released under the Apache License 2.0.
:: The license text is at http://dimdwarf.sourceforge.net/LICENSE


:: Startup script for Dimdwarf. Loads dynamically all JAR files
:: in the library directories.
if %2"" == "" goto :help


:: Configure application parameters and paths
set APP_LIBRARY_DIR=%1
set APP_CONFIG_FILE=%2

set JAVA=java
if not "%JAVA_HOME%" == "" (
    set JAVA=%JAVA_HOME%\bin\java
)
if "%DIMDWARF_HOME%" == "" (
    set DIMDWARF_HOME=.
)


:: Custom JVM options
set VMOPTIONS=
for /f "usebackq delims=" %%G in ("%DIMDWARF_HOME%\dimdwarf.vmoptions") do (call :add_to_vmoptions %%G)


:: Add all JARs in library dirs to classpath
set CP=
for /f %%G in ('dir /b "%DIMDWARF_HOME%\lib\*.jar"') do (call :add_to_classpath "%DIMDWARF_HOME%\lib\%%G")
for /f %%G in ('dir /b "%APP_LIBRARY_DIR%\*.jar"')   do (call :add_to_classpath "%APP_LIBRARY_DIR%\%%G")


:: Start up Dimdwarf
"%JAVA%" %VMOPTIONS% -cp %CP% -javaagent:lib/dimdwarf-aop-agent.jar ^
    net.orfjackal.dimdwarf.server.Startup "%DIMDWARF_HOME%\dimdwarf.properties" "%APP_CONFIG_FILE%"
goto :eof


:: These tricks are needed for Windows's FOR loops to work as expected. See http://www.ss64.com/nt/for.html

:add_to_vmoptions
    set VMOPTIONS=%VMOPTIONS% %1
    goto :eof

:add_to_classpath
    set CP=%CP%;%1
    goto :eof

:help
    echo Usage: dimdwarf APP_LIBRARY_DIR APP_CONFIG_FILE
    echo Starts up the Dimdwarf with the specified application.
    echo All libraries used by the application need to be as JAR files
    echo in the specified library directory.
    echo.
    echo Optional environmental variables:
    echo     DIMDWARF_HOME    Install path of Dimdwarf (default: .)
    echo     JAVA_HOME        Java Runtime Environment to use
    goto :eof

