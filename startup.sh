#!/usr/bin/env bash

exec java -jar \
  -XX:+CrashOnOutOfMemoryError \
  -XX:+HeapDumpOnOutOfMemoryError \
  ${JVM_MEM_ARGS:--Xms16m -Xmx64m} \
  $JVM_ARGS \
  service.jar
