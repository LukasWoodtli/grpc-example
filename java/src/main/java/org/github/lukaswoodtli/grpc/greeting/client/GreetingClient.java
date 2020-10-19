package org.github.lukaswoodtli.grpc.greeting.client;

import com.proto.greet.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client");
        new GreetingClient().run();
    }

    public void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext() // disable SSL (use only for development!)
                .build();

        /* For SSL/TLS (custom CA root certificates):
        ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", 50052)
            .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.cert")).build())
            .build();

        See also:
        https://grpc.io/docs/guides/auth/
        */

        // unary
        doUnaryRequest(channel);

        // Server Streaming
        doStreamingServerRequest(channel);

        // Client streaming
        doClientStreamingRequest(channel);

        // Bi-Directional streaming
        doBiDiStreamingRequest(channel);

        // Error handling
        doErrorRequest(channel);

        // Deadline
        doUnaryCallWithDeadline(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private static void doUnaryRequest(ManagedChannel channel) {
        System.out.println("Creating stub");
        // create blocking greet service client
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // create protobuf greeting message
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Lukas")
                .setLastName("Woodtli")
                .build();

        // create protobuf greet request message
        GreetRequest greetRequest = GreetRequest.newBuilder().setGreeting(greeting).build();

        // call RPC and get a greet response
        GreetResponse greetResponse = greetClient.greet(greetRequest);

        // do something with response
        System.out.println(greetResponse.getResult());
    }

    private static void doStreamingServerRequest(ManagedChannel channel) {
        System.out.println("Creating stub");
        // create blocking greet service client
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Lukas")).build();

        // stream the responses
        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(response -> {
                    System.out.println(response.getResult());
                });
    }

    private static void doClientStreamingRequest(ManagedChannel channel) {
        System.out.println("Creating async stub");
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestStreamObserver = asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                // get response from server
                // inNext is going to be called only once
                System.out.println("Received a response from the server");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                // get error from server
            }

            @Override
            public void onCompleted() {
                // server is done sending data
                System.out.println("The server has completed sending us data");
                latch.countDown();
            }
        });

        System.out.println("Sending message 1");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Lukas").build()).build());

        System.out.println("Sending message 2");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Marcy").build()).build());

        System.out.println("Sending message 3");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("John").build()).build());

        // we tell the server that the client is done
        requestStreamObserver.onCompleted();


        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void doBiDiStreamingRequest(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestStreamObserver = asyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("Response from server: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");
                latch.countDown();
            }
        });

        Arrays.asList("Lukas", "Patricia", "John", "Marcy", "Peter").forEach(
                name -> {
                    System.out.println("Sending: " + name);
                    requestStreamObserver.onNext(GreetEveryoneRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder()
                                    .setFirstName(name))
                            .build());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        requestStreamObserver.onCompleted();
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doErrorRequest(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        String firstName = "";

        try {
            Greeting greeting = Greeting.newBuilder().setFirstName(firstName).build();
            blockingStub.greetWithError(GreetWithErrorRequest.newBuilder().setGreeting(greeting).build());
        } catch (StatusRuntimeException e) {
            System.out.println("Got an error");
            e.printStackTrace();
        }
    }

    private void doUnaryCallWithDeadline(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetServiceBlockingStub = GreetServiceGrpc.newBlockingStub(channel);

        try {
            callWithDeadline(greetServiceBlockingStub, 5000);
            Thread.sleep(1000);
            callWithDeadline(greetServiceBlockingStub, 100);
        }
        catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline exceeded, we are no longer interested in the answer");
            } else {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void callWithDeadline(GreetServiceGrpc.GreetServiceBlockingStub greetServiceBlockingStub, int deadline) {
        System.out.println("Send request with " + deadline + " ms deadline");
        GreetWithDeadlineResponse response = greetServiceBlockingStub.withDeadline(Deadline.after(deadline, TimeUnit.MILLISECONDS))
                .greetWithDeadline(GreetWithDeadlineRequest.newBuilder().setGreeting(Greeting.newBuilder().setFirstName("Lukas").build()).build());

        System.out.println(response.getResult());
    }
}
