#!/bin/bash

JAVA=$1
GERRIT_WAR=$2
GERRIT_HOME=$3
COMMAND="$JAVA -jar $GERRIT_WAR init -d $GERRIT_HOME"

echo "Running: $COMMAND"

STATUS=`exec $COMMAND`

echo $STATUS

exit 0