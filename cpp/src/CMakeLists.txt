

add_executable(greeting_server)

target_sources(greeting_server
        PRIVATE
            main.cpp
            GreetServerImpl.cpp
            GreetServerImpl.h)

target_link_libraries(greeting_server PRIVATE grpc_example_lib)