package com.trademesh.gateway.service;

import com.trademesh.market.grpc.PriceRequest;
import com.trademesh.market.grpc.PriceService;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

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

    /**
     * Triggered on application startup.
     * @param ev Startup event.
     */
    void onStart(@Observes StartupEvent ev) {
        LOG.info("Starting Semantic Warm-up...");

        // Perform warm-up tasks asynchronously to not block the main startup thread
        CompletableFuture.runAsync(() -> {
            try {
                // 1. Connectivity Check / Dummy Calls
                LOG.info("Warming up gRPC connection to market-service...");
                for (int i = 0; i < 5; i++) {
                    marketService.getPrice(PriceRequest.newBuilder().setAssetId("BTC").build())
                        .await().atMost(java.time.Duration.ofSeconds(2));
                }

                // 2. Artificial delay to simulate heavy initialization
                TimeUnit.SECONDS.sleep(2);

                state.setReady();
                LOG.info("Semantic Warm-up completed successfully.");
            } catch (Exception e) {
                LOG.error("Warm-up failed, service will remain UNREADY.", e);
            }
        });
    }
}
