cmake_minimum_required(VERSION 3.17.3)

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


set(GENERATED_FILES_DIR ${PROJECT_BINARY_DIR}/gen_grpc)
function(generate_grpc_code proto_file generated_files_list)
    set(GRPC_FILES_DIR ${PROJECT_SOURCE_DIR}/../java/src/main/proto/greet/)
    file(MAKE_DIRECTORY ${GENERATED_FILES_DIR})

    get_filename_component(GRPC_FILE_BASENAME ${proto_file} NAME_WE CACHE)

    # protoc  --grpc_out=./cpp --plugin=protoc-gen-grpc=`which grpc_cpp_plugin` src/main/proto/greet/greet.proto
    set(GEN_GRPC_CODE ${GENERATED_FILES_DIR}/${GRPC_FILE_BASENAME}.grpc.pb.cc ${GENERATED_FILES_DIR}/${GRPC_FILE_BASENAME}.grpc.pb.h)
    add_custom_command(OUTPUT ${GEN_GRPC_CODE}
                       WORKING_DIRECTORY ${GRPC_FILES_DIR}
                       COMMAND ${PROTOC}
                               --grpc_out=${GENERATED_FILES_DIR}
                               --plugin=protoc-gen-grpc=`which grpc_cpp_plugin`
                               ${proto_file})

    # protoc --cpp_out=./cpp/ src/main/proto/greet/greet.proto
    set(GEN_PROTOBUF_CODE ${GENERATED_FILES_DIR}/${GRPC_FILE_BASENAME}.pb.cc ${GENERATED_FILES_DIR}/${GRPC_FILE_BASENAME}.pb.h)
    add_custom_command(OUTPUT ${GEN_PROTOBUF_CODE}
                       WORKING_DIRECTORY ${GRPC_FILES_DIR}
                       COMMAND ${PROTOC} --cpp_out=${GENERATED_FILES_DIR} ${proto_file})

    set_source_files_properties(${GEN_GRPC_CODE} ${GEN_PROTOBUF_CODE} PROPERTIES GENERATED TRUE)

    set(${generated_files_list} ${${generated_files_list}} ${GEN_GRPC_CODE} ${GEN_PROTOBUF_CODE} PARENT_SCOPE)
endfunction()


generate_grpc_code("greet.proto" generated_files)

add_library(grpc_example_lib SHARED
    ${generated_files})

target_include_directories(grpc_example_lib PUBLIC ${GENERATED_FILES_DIR} ${Protobuf_INCLUDE_DIRS})

target_link_libraries(grpc_example_lib PUBLIC ${Protobuf_LIBRARIES} ${GRPC_LIB})



add_subdirectory(src)