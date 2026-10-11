#!/bin/sh
set -e
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
if [ -x /usr/libexec/java_home ]; then
  JAVA_21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || true)
  if [ -n "$JAVA_21_HOME" ]; then
    JAVA_HOME="$JAVA_21_HOME"
    export JAVA_HOME
  fi
fi
exec java -Dmaven.multiModuleProjectDirectory="$BASE_DIR" -classpath "$BASE_DIR/.mvn/wrapper/maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain "$@"
