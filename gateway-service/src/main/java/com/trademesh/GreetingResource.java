package com.trademesh;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Simple REST endpoint provided as boilerplate by Quarkus.
 */
@Path("/hello")
public class GreetingResource {

    /**
     * Responds with a simple text greeting.
     * @return Greeting text.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }
}
