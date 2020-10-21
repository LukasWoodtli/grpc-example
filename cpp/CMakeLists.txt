cmake_minimum_required(VERSION 3.17.3)

project(grpc-example-cpp)

add_compile_options(-std=c++17)

find_program(PROTOC protoc REQUIRED)
find_program(GRPC_CPP_PLUGIN grpc_cpp_plugin REQUIRED)

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
    find_library(GRPCPP grpc++ REQUIRED)
    set(GRPC_LIB ${GRPC} ${GRPCPP})
endif()


# Need to get googleapis Proto files to support grpc-json-transcoding
include(FetchContent)

FetchContent_Declare(
        googleapis
        GIT_REPOSITORY https://github.com/googleapis/googleapis/
        GIT_TAG        common-protos-1_3_1
)
FetchContent_GetProperties(googleapis)
if(NOT googleapis_POPULATED)
    FetchContent_Populate(googleapis)
endif()

get_filename_component(GOOGLE_APIS_PROTODIR ${googleapis_SOURCE_DIR} ABSOLUTE)

set(GENERATED_FILES_DIR ${PROJECT_BINARY_DIR}/generated_grpc)
function(generate_grpc_code proto_file generated_files_list)
    set(GRPC_FILES_DIR ${PROJECT_SOURCE_DIR}/../java/src/main/proto/greet/)
    file(MAKE_DIRECTORY ${GENERATED_FILES_DIR})

    get_filename_component(GRPC_API_DEFS "${GOOGLE_APIS_PROTODIR}" ABSOLUTE)

    get_filename_component(GRPC_FILE_BASENAME ${proto_file} NAME_WE CACHE)

    # protoc  --grpc_out=./cpp --plugin=protoc-gen-grpc=`which grpc_cpp_plugin` src/main/proto/greet/greet.proto
    # protoc --cpp_out=./cpp/ src/main/proto/greet/greet.proto
    set(GEN_GRPC_CODE ${GENERATED_FILES_DIR}/${GRPC_FILE_BASENAME}.grpc.pb.cc ${GENERATED_FILES_DIR}/${GRPC_FILE_BASENAME}.grpc.pb.h)
    set(GEN_PROTOBUF_CODE ${GENERATED_FILES_DIR}/${GRPC_FILE_BASENAME}.pb.cc ${GENERATED_FILES_DIR}/${GRPC_FILE_BASENAME}.pb.h)
    set(GEN_PROTOBUF_API_FILES
            ${GENERATED_FILES_DIR}/google/api/annotations.grpc.pb.cc
            ${GENERATED_FILES_DIR}/google/api/annotations.grpc.pb.h
            ${GENERATED_FILES_DIR}/google/api/annotations.pb.cc
            ${GENERATED_FILES_DIR}/google/api/annotations.pb.h
            ${GENERATED_FILES_DIR}/google/api/http.grpc.pb.cc
            ${GENERATED_FILES_DIR}/google/api/http.grpc.pb.h
            ${GENERATED_FILES_DIR}/google/api/http.pb.cc
            ${GENERATED_FILES_DIR}/google/api/http.pb.h)
    add_custom_command(OUTPUT ${GEN_GRPC_CODE} ${GEN_PROTOBUF_CODE} ${GEN_PROTOBUF_API_FILES} ${GENERATED_FILES_DIR}/${GRPC_FILE_BASENAME}.pb
                       WORKING_DIRECTORY ${GRPC_FILES_DIR}
                       COMMAND ${PROTOC}
                               -I=${GRPC_API_DEFS}
                               --include_imports
                               --include_source_info
                               --descriptor_set_out=${GENERATED_FILES_DIR}/${GRPC_FILE_BASENAME}.pb
                               --plugin=protoc-gen-grpc=${GRPC_CPP_PLUGIN}
                               --proto_path=${GRPC_FILES_DIR}
                               --grpc_out=${GENERATED_FILES_DIR}
                               --cpp_out=${GENERATED_FILES_DIR}
                               ${GOOGLE_APIS_PROTODIR}/google/api/annotations.proto
                               ${GOOGLE_APIS_PROTODIR}/google/api/http.proto
                               ${proto_file})


    #set(GEN_PROTOBUF_CODE ${GENERATED_FILES_DIR}/${GRPC_FILE_BASENAME}.pb.cc ${GENERATED_FILES_DIR}/${GRPC_FILE_BASENAME}.pb.h)
    #add_custom_command(OUTPUT ${GEN_PROTOBUF_CODE}
    #                   WORKING_DIRECTORY ${GRPC_FILES_DIR}
    #                   COMMAND ${PROTOC}
    #                           --cpp_out=${GENERATED_FILES_DIR}
    #                           ${proto_file})

    set_source_files_properties(${GEN_GRPC_CODE} ${GEN_PROTOBUF_CODE} ${GEN_PROTOBUF_API_FILES} PROPERTIES GENERATED TRUE)

    set(${generated_files_list} ${${generated_files_list}} ${GEN_GRPC_CODE} ${GEN_PROTOBUF_CODE} ${GEN_PROTOBUF_API_FILES} PARENT_SCOPE)
endfunction()


generate_grpc_code("greet.proto" generated_files)

add_library(grpc_example_lib SHARED
    ${generated_files})

target_include_directories(grpc_example_lib PUBLIC ${GENERATED_FILES_DIR} ${Protobuf_INCLUDE_DIRS})

target_link_libraries(grpc_example_lib PUBLIC ${Protobuf_LIBRARIES} ${GRPC_LIB})



add_subdirectory(src)