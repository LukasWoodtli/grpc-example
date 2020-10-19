package org.github.lukaswoodtli.grpc.greeting.client;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestService {

    private static ManagedChannel channel;

    @BeforeAll
    static void setUp() {
        channel = GreetingClient.setupChannel();
    }

    @Test
    void testUnaryRequest() {
        String result = GreetingClient.doUnaryRequestImpl(channel);
        assertEquals("Hello Lukas form C++", result);
    }

    @Test
    void testStreamingServerRequest() {
        List<String> expected = new ArrayList<String>();
        for(int i = 0; i < 10; ++i) {
            expected.add("Hello Lukas, response No: " + i);
        }

        List<String> result = GreetingClient.doStreamingServerRequest(channel);
        assertIterableEquals(expected, result);
    }

    @Test
    void testClientStreamingRequest() {
        String expected = "Hello Lukas from C++\n" +
                "Hello Marcy from C++\n" +
                "Hello John from C++\n" +
                "Client finished sending data\n";
        String result = GreetingClient.doClientStreamingRequest(channel);
        assertEquals(expected, result);
    }

    @Test
    void testBiDiStreamingRequest() {
        List<String> expected  = Arrays.asList("Lukas", "Patricia", "John", "Marcy", "Peter")
                .stream().map(name -> { return "Echoing name " + name + " from C++"; })
                .collect(Collectors.toList());

        List<String> result = GreetingClient.doBiDiStreamingRequest(channel);
        assertIterableEquals(expected, result);
    }

    @Test
    void testErrorRequest() {
        assertThrows(StatusRuntimeException.class, () -> GreetingClient.doErrorRequestImpl(channel));
    }

    @Test
    void testUnaryCallWithDeadlineNoTimeout() {
        GreetingClient.doUnaryCallWithDeadlineImpl(channel, 5000);
    }

    @Test
    void testUnaryCallWithDeadlineTimeout() {
        assertThrows(StatusRuntimeException.class,
                () -> GreetingClient.doUnaryCallWithDeadlineImpl(channel, 100));
    }
}

