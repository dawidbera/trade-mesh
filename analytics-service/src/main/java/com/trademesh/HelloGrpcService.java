package com.trademesh;

import io.quarkus.grpc.GrpcService;

import io.smallrye.mutiny.Uni;

/**
 * Boilerplate gRPC service example provided by Quarkus.
 */
@GrpcService
public class HelloGrpcService implements HelloGrpc {

    /**
     * Responds with a hello message.
     * @param request The hello request containing a name.
     * @return A Uni emitting the hello reply.
     */
    @Override
    public Uni<HelloReply> sayHello(HelloRequest request) {
        return Uni.createFrom().item("Hello " + request.getName() + "!")
                .map(msg -> HelloReply.newBuilder().setMessage(msg).build());
    }
    
}
