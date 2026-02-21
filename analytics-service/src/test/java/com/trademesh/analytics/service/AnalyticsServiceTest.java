package com.trademesh.analytics.service;

import com.trademesh.analytics.grpc.IndicatorRequest;
import com.trademesh.analytics.grpc.IndicatorResponse;
import com.trademesh.market.model.MarketPrice;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AnalyticsServiceBean.
 * Verifies the interaction between RabbitMQ consumption, Redis storage, and gRPC retrieval.
 * Quarkus automatically starts a Redis container for these tests.
 */
@QuarkusTest
public class AnalyticsServiceTest {

    @Inject
    @io.quarkus.grpc.GrpcService
    AnalyticsServiceBean analyticsService;

    /**
     * Tests that the Simple Moving Average (SMA) is calculated correctly after 
     * multiple price updates are processed via the RabbitMQ consumer.
     */
    @Test
    @DisplayName("Should calculate SMA correctly after receiving prices via RabbitMQ")
    public void testSmaCalculation() {
        String assetId = "BTC";
        
        // 1. Simulate receiving prices through the RabbitMQ consumer method
        analyticsService.consumePrice(new MarketPrice(assetId, 100.0));
        analyticsService.consumePrice(new MarketPrice(assetId, 200.0));

        // 2. Request SMA with period 2
        IndicatorRequest request = IndicatorRequest.newBuilder()
                .setAssetId(assetId)
                .setIndicatorType("SMA")
                .setPeriod(2)
                .build();

        // 3. Verify the result using blocking await for test simplicity
        IndicatorResponse response = analyticsService.getIndicator(request)
                .await().indefinitely();

        assertThat(response.getAssetId()).isEqualTo(assetId);
        assertThat(response.getIndicatorType()).isEqualTo("SMA");
        assertThat(response.getValue()).isEqualTo(150.0); // Average of 100 and 200
    }

    /**
     * Verifies that the service returns a random indicator value for types 
     * other than SMA, acting as a placeholder for future implementations.
     */
    @Test
    @DisplayName("Should return random value for unknown indicators")
    public void testUnknownIndicator() {
        IndicatorRequest request = IndicatorRequest.newBuilder()
                .setAssetId("ETH")
                .setIndicatorType("RSI")
                .setPeriod(14)
                .build();

        IndicatorResponse response = analyticsService.getIndicator(request)
                .await().indefinitely();

        assertThat(response.getValue()).isBetween(0.0, 100.0);
    }
}
