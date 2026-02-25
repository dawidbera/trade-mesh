package com.trademesh.history.service;

import io.agroal.api.AgroalDataSource;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Executes the semantic warm-up sequence for the History service.
 * Performs a dummy DB check to ensure the connection pool is initialized.
 */
@ApplicationScoped
public class WarmupExecutor {

    private static final Logger LOG = Logger.getLogger(WarmupExecutor.class);

    @Inject
    WarmupState state;

    @Inject
    AgroalDataSource dataSource;

    /**
     * Triggered on application startup to initiate the warm-up sequence.
     * @param ev Startup event.
     */
    void onStart(@Observes StartupEvent ev) {
        LOG.info("Starting Semantic Warm-up for History Service...");

        CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                LOG.info("Warming up database connection pool...");
                stmt.executeQuery("SELECT 1").next();

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
