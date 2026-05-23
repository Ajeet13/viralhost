#!/bin/sh
#
# Gradle start up script for POSIX systems. Stub – Android Studio will replace
# this file (and add gradle-wrapper.jar) on first sync. For CLI builds, run
# `gradle wrapper` once with a local Gradle install.
#

DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$DIR"

JAVACMD=${JAVA_HOME:+$JAVA_HOME/bin/java}
[ -z "$JAVACMD" ] && JAVACMD=java

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
exec "$JAVACMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
