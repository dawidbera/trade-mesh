package com.trademesh.analytics.service;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Executes the semantic warm-up sequence for the Analytics service.
 * Performs a dummy Redis operation to ensure the connection pool is initialized.
 */
@ApplicationScoped
public class WarmupExecutor {

    private static final Logger LOG = Logger.getLogger(WarmupExecutor.class);

    @Inject
    WarmupState state;

    @Inject
    RedisDataSource ds;

    /**
     * Triggered on application startup to initiate the warm-up sequence.
     * @param ev Startup event.
     */
    void onStart(@Observes StartupEvent ev) {
        LOG.info("Starting Semantic Warm-up for Analytics Service...");

        CompletableFuture.runAsync(() -> {
            try {
                // 1. Redis Connectivity Check
                LOG.info("Warming up Redis connection pool...");
                ds.execute("PING");

                // 2. Delay
                TimeUnit.SECONDS.sleep(1);

                state.setReady();
                LOG.info("Analytics Service Semantic Warm-up completed.");
            } catch (Exception e) {
                LOG.error("Analytics Service Warm-up failed.", e);
            }
        });
    }
}
