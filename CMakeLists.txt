cmake_minimum_required(VERSION 3.17.4)

project(grpc-example-cpp)

add_compile_options(-std=c++17)

find_program(PROTOC protoc REQUIRED)

if (APPLE)
    # This is a bug in CMake that causes it to prefer the system version over
    # the one in the specified ROOT folder.
    set(OPENSSL_ROOT_DIR ${OPENSSL_ROOT_DIR} /usr/local/opt/openssl/)
    set(OPENSSL_CRYPTO_LIBRARY ${OPENSSL_ROOT_DIR}/lib/libcrypto.dylib CACHE FILEPATH "" FORCE)
    set(OPENSSL_SSL_LIBRARY ${OPENSSL_ROOT_DIR}/lib/libssl.dylib CACHE FILEPATH "" FORCE)
endif()
find_package(OpenSSL REQUIRED)

find_package(Protobuf REQUIRED)

if (APPLE)
    find_package(gRPC CONFIG REQUIRED)
    set(GRPC_LIB gRPC::grpc++)
else()
    find_library(GRPC grpc REQUIRED)
    set(GRPC_LIB ${GRPC})
endif()

file(MAKE_DIRECTORY cpp)

# protoc  --grpc_out=./cpp --plugin=protoc-gen-grpc=`which grpc_cpp_plugin` src/main/proto/greet/greet.proto
add_custom_command(OUTPUT ${PROJECT_SOURCE_DIR}/cpp/greet.grpc.pb.cc ${PROJECT_SOURCE_DIR}/cpp/greet.grpc.pb.h
                   WORKING_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/proto/greet/
                   COMMAND ${PROTOC} --grpc_out=${PROJECT_SOURCE_DIR}/cpp --plugin=protoc-gen-grpc=`which grpc_cpp_plugin` greet.proto)

# protoc --cpp_out=./cpp/ src/main/proto/greet/greet.proto
add_custom_command(OUTPUT ${PROJECT_SOURCE_DIR}/cpp/greet.pb.cc ${PROJECT_SOURCE_DIR}/cpp/greet.pb.h
                   WORKING_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/proto/greet/
                   COMMAND ${PROTOC} --cpp_out=${PROJECT_SOURCE_DIR}/cpp greet.proto)


set_source_files_properties(
    ${PROJECT_SOURCE_DIR}/cpp/greet.grpc.pb.cc
    ${PROJECT_SOURCE_DIR}/cpp/greet.grpc.pb.h
    ${PROJECT_SOURCE_DIR}/cpp/greet.pb.cc
    ${PROJECT_SOURCE_DIR}/cpp/greet.pb.h
    PROPERTIES GENERATED TRUE)

add_library(grpc_example SHARED
    ${PROJECT_SOURCE_DIR}/cpp/greet.grpc.pb.cc
    ${PROJECT_SOURCE_DIR}/cpp/greet.grpc.pb.h
    ${PROJECT_SOURCE_DIR}/cpp/greet.pb.cc
    ${PROJECT_SOURCE_DIR}/cpp/greet.pb.h)

target_include_directories(grpc_example PRIVATE ${PROJECT_SOURCE_DIR}/cpp/ ${Protobuf_INCLUDE_DIRS})

target_link_libraries(grpc_example PRIVATE ${Protobuf_LIBRARIES} ${GRPC_LIB})
