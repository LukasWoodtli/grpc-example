#include <sstream>
#include <chrono>
#include <thread>

#include "GreetServerImpl.h"

// Unary
grpc::Status GreetServerImpl::Greet(::grpc::ServerContext *context,
                                    const ::greet::GreetRequest *request,
                                    ::greet::GreetResponse *response) {

  const auto& firstName = request->greeting().first_name();
  response->set_result("Hello " + firstName + " form C++");

  return grpc::Status::OK;
}

// Server Streaming
grpc::Status GreetServerImpl::GreetManyTimes(
    ::grpc::ServerContext *context,
    const ::greet::GreetManyTimesRequest *request,
    ::grpc::ServerWriter<::greet::GreetManyTimesResponse> *writer) {

  const auto& firstName = request->greeting().first_name();

  for (auto i=0; i<10; ++i) {
    const auto result = "Hello " + firstName + ", response No: " + std::to_string(i);

    ::greet::GreetManyTimesResponse response;
    response.set_result(result);
    writer->Write(response);
  }

  return grpc::Status::OK;
}

// Client Streaming
grpc::Status GreetServerImpl::LongGreet(
    ::grpc::ServerContext *context,
    ::grpc::ServerReader<::greet::LongGreetRequest> *reader,
    ::greet::LongGreetResponse *response) {

  std::stringstream answer;

  ::greet::LongGreetRequest request;
  while (reader->Read(&request)) {
    const auto firstName = request.greeting().first_name();

    answer << "Hello " << firstName << " from C++\n";
  }

  answer << "Client finished sending data\n";

  response->set_result(answer.str());

  return grpc::Status::OK;
}

// Bi-Directional Streaming
grpc::Status GreetServerImpl::GreetEveryone(
    ::grpc::ServerContext *context,
    ::grpc::ServerReaderWriter<::greet::GreetEveryoneResponse,
                               ::greet::GreetEveryoneRequest> *stream) {

  greet::GreetEveryoneRequest request;
  while(stream->Read(&request)) {
    const auto firstName = request.greeting().first_name();
    ::greet::GreetEveryoneResponse response;
    response.set_result("Echoing name " + firstName + " from C++");
    stream->Write(response);
  }

  return grpc::Status::OK;
}


grpc::Status
GreetServerImpl::GreetWithError(::grpc::ServerContext *context,
                                const ::greet::GreetWithErrorRequest *request,
                                ::greet::GreetWithErrorResponse *response) {

  if (not request->greeting().first_name().empty()) {
    const auto& firstName = request->greeting().first_name();
    response->set_result("Hello " + firstName + " form C++");
    return grpc::Status::OK;
  }
  else {
    // s.a. http://avi.im/grpc-errors/
    return grpc::Status(grpc::StatusCode::INVALID_ARGUMENT,
                        "Empty name sent (from C++");
  }
}


grpc::Status GreetServerImpl::GreetWithDeadline(
    ::grpc::ServerContext *context,
    const ::greet::GreetWithDeadlineRequest *request,
    ::greet::GreetWithDeadlineResponse *response) {

  for (int i=0; i<3; ++i) {
    if(not context->IsCancelled()) {
      std::cout << "Sleeping for 100 ms";
      std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }
    else {
      // Doesn't matter what we return here. Deadline is exceeded on the client.
      return grpc::Status();
    }
  }

  const auto& firstName = request->greeting().first_name();
  response->set_result("Hello " + firstName + " form C++");
  return grpc::Status::OK;
}
