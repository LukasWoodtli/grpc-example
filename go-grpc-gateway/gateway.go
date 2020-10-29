package main

import (
	"context"
	"flag"
	"fmt"
	gateway "go-grpc-gateway/gen/go"
	"net/http"

	"github.com/golang/glog"
	"github.com/grpc-ecosystem/grpc-gateway/runtime"
	"google.golang.org/grpc"
)

const grpcServerHostAndPort = "localhost:50052"

var (
	// command-line options:
	// gRPC server endpoint
	grpcServerEndpoint = flag.String("grpc-server-endpoint", grpcServerHostAndPort, "gRPC server endpoint")
)

func run() error {
	ctx := context.Background()
	ctx, cancel := context.WithCancel(ctx)
	defer cancel()

	// Register gRPC server endpoint
	// Note: Make sure the gRPC server is running properly and accessible
	mux := runtime.NewServeMux()
	opts := []grpc.DialOption{grpc.WithInsecure()}
	err := gateway.RegisterGreetServiceHandlerFromEndpoint(ctx, mux, *grpcServerEndpoint, opts)
	if err != nil {
		return err
	}

	const gatewayPort = ":8081"
	fmt.Println("Starting gRPC-JSON gateway on " + gatewayPort)
	fmt.Println("Expecting gRPC server on: " + grpcServerHostAndPort)
	// Start HTTP server (and proxy calls to gRPC server endpoint)
	return http.ListenAndServe(gatewayPort, mux)
}

func main() {
	flag.Parse()
	defer glog.Flush()

	if err := run(); err != nil {
		glog.Fatal(err)
	}
}
