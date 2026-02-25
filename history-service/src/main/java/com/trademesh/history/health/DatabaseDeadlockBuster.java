package com.trademesh.history.health;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * Self-healing component that monitors the Agroal connection pool for deadlocks or exhaustion.
 * Automatically signals OpenShift to restart the Pod if the pool becomes unhealthy.
 */
@Liveness
@ApplicationScoped
public class DatabaseDeadlockBuster implements HealthCheck {

    @Inject
    AgroalDataSource dataSource;

    /**
     * Monitors the health of the connection pool.
     * @return DOWN if threads are stuck waiting for connections.
     */
    @Override
    public HealthCheckResponse call() {
        var metrics = dataSource.getMetrics();
        var config = dataSource.getConfiguration().connectionPoolConfiguration();

        long activeConnections = metrics.activeCount();
        long maxConnections = config.maxSize();
        long awaitingThreads = metrics.awaitingCount();

        // Threshold: If we are at max capacity AND threads are awaiting for too long
        // or the ratio of awaiting threads is dangerously high.
        boolean isPoolExhausted = activeConnections >= maxConnections && awaitingThreads > (maxConnections * 0.5);
        
        if (isPoolExhausted) {
            return HealthCheckResponse.builder()
                .name("Database Deadlock Buster")
                .down()
                .withData("activeConnections", activeConnections)
                .withData("maxConnections", maxConnections)
                .withData("awaitingThreads", awaitingThreads)
                .withData("reason", "Connection pool exhausted/deadlocked")
                .build();
        }

        return HealthCheckResponse.builder()
            .name("Database Deadlock Buster")
            .up()
            .withData("activeConnections", activeConnections)
            .withData("maxConnections", maxConnections)
            .withData("awaitingThreads", awaitingThreads)
            .build();
    }
}
