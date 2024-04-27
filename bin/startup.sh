#!/bin/bash

# Environment variables.
JAVA_CMD=$(which java)

# Start command with the OpenTelemetry agent.
$JAVA_CMD -jar "$LIBS_DIR"/prometheus2json.jar "$1"