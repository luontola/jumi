#!/bin/sh
# Copyright (c) 2008-2010 Esko Luontola <www.orfjackal.net>
# This software is released under the Apache License 2.0.
# The license text is at http://dimdwarf.sourceforge.net/LICENSE


# Main script for Dimdwarf. Loads dynamically all JAR files
# in the library directories.
if [ $# -ne 2 ]; then
    echo "Usage: dimdwarf APP_LIBRARY_DIR APP_CONFIG_FILE"
    echo "Starts up the Dimdwarf with the specified application."
    echo "All libraries used by the application need to be as JAR files"
    echo "in the specified library directory."
    echo
    echo "Optional environmental variables:"
    echo "    DIMDWARF_HOME    Install path of Dimdwarf (default: .)"
    echo "    JAVA_HOME        Java Runtime Environment to use"
    exit 1
fi


# Configure application parameters and paths
APP_LIBRARY_DIR="$1"
APP_CONFIG_FILE="$2"

JAVA=java
if [ -n "$JAVA_HOME" ]; then
    JAVA=$JAVA_HOME/bin/java
fi
if [ -z "$DIMDWARF_HOME" ]; then
    DIMDWARF_HOME="."
fi


# Custom JVM options (comments start with ";")
VMOPTIONS=`sed -e 's/;.*$//' "$DIMDWARF_HOME/dimdwarf.vmoptions"`


# Add all JARs in library dirs to classpath
CP=
for FILE in $DIMDWARF_HOME/lib/*.jar; do
    CP=$CP$PATHSEP$FILE
done
for FILE in $APP_LIBRARY_DIR/*.jar; do
    CP=$CP$PATHSEP$FILE
done


# Start up Dimdwarf
"$JAVA" $VMOPTIONS -cp $CP -javaagent:lib/dimdwarf-aop-agent.jar \
    net.orfjackal.dimdwarf.server.Startup "$DIMDWARF_HOME/dimdwarf.properties" "$APP_CONFIG_FILE"
