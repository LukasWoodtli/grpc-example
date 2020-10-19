#pragma once

#include "greet.grpc.pb.h"

class GreetServerImpl : public greet::GreetService::Service {
public:
  grpc::Status Greet(::grpc::ServerContext *context,
                     const ::greet::GreetRequest *request,
                     ::greet::GreetResponse *response) override;
  grpc::Status GreetManyTimes(
      ::grpc::ServerContext *context,
      const ::greet::GreetManyTimesRequest *request,
      ::grpc::ServerWriter<::greet::GreetManyTimesResponse> *writer) override;
  grpc::Status
  LongGreet(::grpc::ServerContext *context,
            ::grpc::ServerReader<::greet::LongGreetRequest> *reader,
            ::greet::LongGreetResponse *response) override;
  grpc::Status
  GreetEveryone(::grpc::ServerContext *context,
                ::grpc::ServerReaderWriter<::greet::GreetEveryoneResponse,
                                           ::greet::GreetEveryoneRequest>
                    *stream) override;
  grpc::Status
  GreetWithError(::grpc::ServerContext *context,
                 const ::greet::GreetWithErrorRequest *request,
                 ::greet::GreetWithErrorResponse *response) override;
  grpc::Status
  GreetWithDeadline(::grpc::ServerContext *context,
                    const ::greet::GreetWithDeadlineRequest *request,
                    ::greet::GreetWithDeadlineResponse *response) override;
};
