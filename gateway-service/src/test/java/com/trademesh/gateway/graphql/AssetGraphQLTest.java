package com.trademesh.gateway.graphql;

import com.trademesh.market.grpc.PriceResponse;
import com.trademesh.market.grpc.PriceService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;

/**
 * Integration tests for the GraphQL Gateway resolvers.
 * Mocks the internal gRPC services to verify the aggregation logic.
 */
@QuarkusTest
public class AssetGraphQLTest {

    @InjectMock
    @io.quarkus.grpc.GrpcClient("market")
    PriceService marketService;

    /**
     * Tests the GraphQL aggregation logic by mocking downstream gRPC services.
     * Verifies that the gateway correctly resolves nested asset fields.
     */
    @Test
    @TestSecurity(user = "testUser", roles = {"user"})
    @DisplayName("Should fetch asset with price via GraphQL")
    public void testAssetQuery() {
        // 1. Mock gRPC response from market-service
        PriceResponse mockResponse = PriceResponse.newBuilder()
                .setAssetId("BTC")
                .setValue(50000.0)
                .setCurrency("USD")
                .setTimestamp(System.currentTimeMillis())
                .build();
        
        Mockito.when(marketService.getPrice(any())).thenReturn(Uni.createFrom().item(mockResponse));

        // 2. Execute GraphQL Query
        String query = """
            {
              "query": "{ asset(id: \\"BTC\\") { id symbol currentPrice { value currency } } }"
            }
            """;

        given()
            .contentType("application/json")
            .body(query)
            .when().post("/graphql")
            .then()
            .statusCode(200)
            .body("data.asset.id", is("BTC"))
            .body("data.asset.currentPrice.value", is(50000.0f));
    }
}
