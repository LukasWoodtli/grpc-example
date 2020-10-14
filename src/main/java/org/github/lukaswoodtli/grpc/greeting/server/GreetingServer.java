package org.github.lukaswoodtli.grpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GreetingServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello gRPC");

        Server server = ServerBuilder.forPort(50052)
                .addService(new GreetServiceImpl())
                /* For TLS/SSL:
                   Generate certificates first
                    (see: https://github.com/grpc/grpc-java/blob/master/testing/src/main/resources/certs/README)
                    then add them here:
                   `.useTransportSecurity(new File("ssl/server.crt"), new File("ssl/server.pem"))` */
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Successfully stopped the server");

        }));

        server.awaitTermination();
    }
}
