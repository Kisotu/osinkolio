#!/bin/bash
# mock-server-stop.sh - Stop WireMock server

set -e

if [ -f "./wiremock/wiremock.pid" ]; then
    echo "Stopping WireMock server..."
    kill $(cat ./wiremock/wiremock.pid)
    rm ./wiremock/wiremock.pid
    echo "WireMock server stopped."
else
    echo "WireMock server is not running."
fi