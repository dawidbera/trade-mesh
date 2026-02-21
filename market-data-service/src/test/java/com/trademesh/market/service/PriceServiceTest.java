package com.trademesh.market.service;

import com.trademesh.market.grpc.PriceRequest;
import com.trademesh.market.grpc.PriceResponse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PriceServiceBean.
 * Verifies that the gRPC service correctly retrieves and simulates asset prices.
 * Quarkus automatically starts a Redis container.
 */
@QuarkusTest
public class PriceServiceTest {

    @Inject
    @io.quarkus.grpc.GrpcService
    PriceServiceBean priceService;

    /**
     * Verifies that the PriceService returns a valid, non-zero price for a 
     * known asset ID.
     */
    @Test
    @DisplayName("Should retrieve a valid price for a known asset")
    public void testGetPrice() {
        String assetId = "BTC";
        PriceRequest request = PriceRequest.newBuilder().setAssetId(assetId).build();

        PriceResponse response = priceService.getPrice(request)
                .await().indefinitely();

        assertThat(response.getAssetId()).isEqualTo(assetId);
        assertThat(response.getValue()).isGreaterThan(0.0);
        assertThat(response.getCurrency()).isEqualTo("USD");
    }
}
