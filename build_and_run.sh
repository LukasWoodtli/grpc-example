#!/bin/bash

set -u
set -e

SCRIPTPATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

cd $SCRIPTPATH


# C++ Server
echo "Build and run C++ server"
pushd cpp
cmake -S . -B build && cmake --build build
./build/src/greeting_server &
CPP_SERVER_PID=$!
popd

# Go Gateway
echo "Build and run gateway"
pushd go-grpc-gateway
make all
./gateway &
GO_GATEWAY_PID=$!
pushd

# Run Java client to test server and gateway
echo "Build and run Java tests"
pushd java
./gradlew --no-daemon check
pushd

kill -9 $CPP_SERVER_PID
kill -9 $GO_GATEWAY_PID
