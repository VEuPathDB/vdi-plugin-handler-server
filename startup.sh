#!/usr/bin/env bash

#R -e "Rserve::Rserve(debug = "${DEBUG}", args=\"--vanilla\")" &
java -jar -XX:+CrashOnOutOfMemoryError $JVM_MEM_ARGS $JVM_ARGS service.jar
