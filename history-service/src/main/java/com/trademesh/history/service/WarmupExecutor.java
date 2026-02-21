package com.trademesh.history.service;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Executes the semantic warm-up sequence for the History service.
 * Performs a dummy DB query to ensure the connection pool is initialized.
 */
@ApplicationScoped
public class WarmupExecutor {

    private static final Logger LOG = Logger.getLogger(WarmupExecutor.class);

    @Inject
    WarmupState state;

    @Inject
    EntityManager em;

    /**
     * Triggered on application startup to initiate the warm-up sequence.
     * Performs a dummy SQL query to initialize the database connection pool.
     * @param ev Startup event.
     */
    void onStart(@Observes StartupEvent ev) {
        LOG.info("Starting Semantic Warm-up for History Service...");

        CompletableFuture.runAsync(() -> {
            try {
                // 1. Database Connectivity Check
                LOG.info("Warming up database connection pool...");
                em.createNativeQuery("SELECT 1").getSingleResult();

                // 2. Delay
                TimeUnit.SECONDS.sleep(1);

                state.setReady();
                LOG.info("History Service Semantic Warm-up completed.");
            } catch (Exception e) {
                LOG.error("History Service Warm-up failed.", e);
            }
        });
    }
}
