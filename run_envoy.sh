#!/bin/bash

docker run -it --rm --name envoy  \
             -v "$(pwd)/cpp/cmake-build-debug/gen_grpc/greet.pb:/data/greet.pb:ro" \
             -v "$(pwd)/envoy-config.yml:/etc/envoy/envoy.yaml:ro" \
             -p 9901:9901 -p 50000:50000 envoyproxy/envoy:v1.12.7
             