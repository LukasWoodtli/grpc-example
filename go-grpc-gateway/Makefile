

all: build_gateway

.PHONY: get_deps
get_deps:
	go get -u github.com/grpc-ecosystem/grpc-gateway/protoc-gen-grpc-gateway
	go get -u github.com/golang/protobuf/protoc-gen-go
	go get -u google.golang.org/grpc/codes
	go mod download


gen_grpc_code: get_deps
	mkdir -p gen/go
	PATH="${PATH}:$(shell go env GOPATH)/bin" protoc -I/usr/local/include -I. \
		-I../java/src/main/proto/greet \
		-I$(shell go env GOPATH) \
		-I$(shell go env GOPATH)/pkg/mod/github.com/grpc-ecosystem/grpc-gateway/v2@v2.0.1/third_party/googleapis \
		--go_out=plugins=grpc:./gen/go \
		--grpc-gateway_out ./gen/go \
		--grpc-gateway_opt logtostderr=true \
		--grpc-gateway_opt paths=source_relative \
		../java/src/main/proto/greet/greet.proto

build_gateway: gen_grpc_code
	go build gateway.go

.PHONY: clean
clean:
	rm -rf gen
	rm gateway