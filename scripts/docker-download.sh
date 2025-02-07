#!/usr/bin/env bash

declare -r TARGET_VERSION=##VERSION_TEMPLATE##

# Download service jar
curl -LOsf "https://github.com/VEuPathDB/vdi-plugin-handler-server/releases/download/${TARGET_VERSION}/service.jar" \
  || { echo "Failed to download service.jar" >&2 ; exit 1 ; }
curl -LOsf "https://github.com/VEuPathDB/vdi-plugin-handler-server/releases/download/${TARGET_VERSION}/service.jar.sha256" \
  || { echo "Failed to download service.jar checksum" >&2 ; exit 1 ; }
sha256sum service.jar | grep -q "$(cat service.jar.sha256)" \
  || { echo "Checksum mismatch on service.jar" >&2 ; exit 1 ; }
rm service.jar.sha256

# Download startup script
curl -LOsf"https://raw.githubusercontent.com/VEuPathDB/vdi-plugin-handler-server/refs/tags/${TARGET_VERSION}/startup.sh" \
  || { echo "Failed to download startup script" >&2 ; exit 1 ; }
chmod +x startup.sh
