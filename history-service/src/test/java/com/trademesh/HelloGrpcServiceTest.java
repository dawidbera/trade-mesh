package com.trademesh;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

/**
 * Tests the HelloGrpcService boilerplate in history-service.
 */
@QuarkusTest
class HelloGrpcServiceTest {
    @GrpcClient
    HelloGrpc helloGrpc;

    /**
     * Verifies the basic functionality of the hello gRPC service.
     */
    @Test
    void testHello() {
        HelloReply reply = helloGrpc
                .sayHello(HelloRequest.newBuilder().setName("Neo").build()).await().atMost(Duration.ofSeconds(5));
        assertEquals("Hello Neo!", reply.getMessage());
    }

}
