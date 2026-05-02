#!/bin/bash
# mock-server.sh - Start standalone WireMock server for API mocking

set -e

echo "Starting WireMock server..."

# Check if wiremock-standalone.jar exists, if not download it
if [ ! -f "./wiremock/wiremock-standalone.jar" ]; then
    echo "Downloading WireMock standalone jar..."
    mkdir -p ./wiremock
    curl -L -o ./wiremock/wiremock-standalone.jar https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-jre8/3.0.1/wiremock-jre8-3.0.1-standalone.jar
fi

# Start WireMock server
java -jar ./wiremock/wiremock-standalone.jar \
    --port 8089 \
    --root-dir ./wiremock \
    --verbose \
    &
    
echo "WireMock server started on port 8089"
echo "To stop: kill %1 or run './scripts/mock-server-stop.sh'"

# Save the PID for potential stopping
echo $! > ./wiremock/wiremock.pid