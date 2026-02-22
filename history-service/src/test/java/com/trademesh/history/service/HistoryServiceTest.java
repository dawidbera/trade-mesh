package com.trademesh.history.service;

import com.trademesh.history.grpc.HistoryQueryRequest;
import com.trademesh.history.grpc.HistoryQueryResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for HistoryServiceBean.
 * Verifies that market prices received via RabbitMQ are correctly archived.
 * Quarkus automatically starts PostgreSQL and RabbitMQ containers.
 */
@QuarkusTest
public class HistoryServiceTest {

    @Inject
    @io.quarkus.grpc.GrpcService
    HistoryServiceBean historyService;

    /**
     * Tests the archival of market prices and subsequent retrieval of historical data.
     * Verifies that the service processes price updates and provides OHLC data.
     */
    @Test
    @DisplayName("Should archive price update and return mock historical data")
    public void testArchivalAndQuery() {
        String assetId = "AAPL";
        
        // 1. Simulate receiving a price update via the RabbitMQ consumer using JsonObject
        historyService.archivePrice(new JsonObject().put("assetId", assetId).put("value", 150.0).put("timestamp", System.currentTimeMillis()));

        // 2. Query historical data (Phase 3 returns mock data)
        HistoryQueryRequest request = HistoryQueryRequest.newBuilder()
                .setAssetId(assetId)
                .setStartTimestamp(Instant.now().minusSeconds(3600).toEpochMilli())
                .setEndTimestamp(Instant.now().toEpochMilli())
                .build();

        HistoryQueryResponse response = historyService.getHistoricalData(request)
                .await().indefinitely();

        // 3. Verify
        assertThat(response.getAssetId()).isEqualTo(assetId);
        assertThat(response.getSeriesList()).isNotEmpty();
    }
}
