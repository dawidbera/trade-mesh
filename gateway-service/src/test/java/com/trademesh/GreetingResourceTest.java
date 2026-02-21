package com.trademesh;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

/**
 * Tests the GreetingResource REST endpoint.
 */
@QuarkusTest
class GreetingResourceTest {
    /**
     * Verifies that the /hello endpoint returns the expected greeting message.
     */
    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("Hello from Quarkus REST"));
    }

}