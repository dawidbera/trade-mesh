package com.trademesh;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * Simple liveness health check provided by Quarkus.
 * In production, this should include logic to detect application staleness or failure.
 */
@Liveness
public class MyLivenessCheck implements HealthCheck {

    /**
     * Responds with the status of the application liveness.
     * @return UP status.
     */
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("alive");
    }

}