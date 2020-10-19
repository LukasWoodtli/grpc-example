#include "GreetServerImpl.h"

#include <grpc/grpc.h>
#include <grpcpp/server.h>
#include <grpcpp/server_builder.h>
#include <grpcpp/server_context.h>



int main(int argc, char** argv) {


    std::string server_address("0.0.0.0:50052");
    GreetServerImpl service{};

    grpc::ServerBuilder builder;
    builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
    builder.RegisterService(&service);
    std::unique_ptr<grpc::Server> server(builder.BuildAndStart());
    std::cout << "C++ Server listening on " << server_address << std::endl;
    server->Wait();

  return 0;
}