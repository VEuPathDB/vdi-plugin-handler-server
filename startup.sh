#!/usr/bin/env bash

exec java -jar -XX:+CrashOnOutOfMemoryError $JVM_MEM_ARGS $JVM_ARGS service.jar
