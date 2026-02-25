package com.trademesh.gateway.service;

import com.trademesh.analytics.grpc.AnalyticsService;
import com.trademesh.analytics.grpc.IndicatorRequest;
import com.trademesh.history.grpc.HistoryQueryRequest;
import com.trademesh.history.grpc.HistoryService;
import com.trademesh.market.grpc.PriceRequest;
import com.trademesh.market.grpc.PriceService;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Executes the semantic warm-up sequence for the Gateway service.
 * Pings internal gRPC services to ensure connectivity and trigger JIT compilation.
 */
@ApplicationScoped
public class WarmupExecutor {

    private static final Logger LOG = Logger.getLogger(WarmupExecutor.class);

    @Inject
    WarmupState state;

    @GrpcClient("market")
    PriceService marketService;

    @GrpcClient("analytics")
    AnalyticsService analyticsService;

    @GrpcClient("history")
    HistoryService historyService;

    /**
     * Triggered on application startup.
     * @param ev Startup event.
     */
    void onStart(@Observes StartupEvent ev) {
        LOG.info("Starting Semantic Warm-up for Gateway Service...");

        // Perform warm-up tasks asynchronously to not block the main startup thread
        CompletableFuture.runAsync(() -> {
            try {
                // 1. Connectivity Check / Dummy Calls to all gRPC clients
                LOG.info("Warming up gRPC connections...");

                for (int i = 0; i < 5; i++) {
                    // Market Service
                    marketService.getPrice(PriceRequest.newBuilder().setAssetId("BTC").build())
                        .await().atMost(java.time.Duration.ofSeconds(2));
                    // Analytics Service
                    analyticsService.getIndicator(IndicatorRequest.newBuilder().setAssetId("BTC").setIndicatorType("SMA").setPeriod(14).build())
                        .await().atMost(java.time.Duration.ofSeconds(2));
                    // History Service
                    historyService.getHistoricalData(HistoryQueryRequest.newBuilder()
                            .setAssetId("BTC")
                            .setStartTimestamp(Instant.now().minusSeconds(3600).toEpochMilli())
                            .setEndTimestamp(Instant.now().toEpochMilli())
                            .build())
                        .await().atMost(java.time.Duration.ofSeconds(2));
                }

                // 2. Artificial delay to simulate heavy initialization
                TimeUnit.SECONDS.sleep(2);

                state.setReady();
                LOG.info("Gateway Service Semantic Warm-up completed successfully.");
            } catch (Exception e) {
                LOG.error("Gateway Service Warm-up failed, service will remain UNREADY.", e);
            }
        });
    }
}
