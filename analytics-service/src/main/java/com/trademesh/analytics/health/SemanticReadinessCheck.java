package com.trademesh.analytics.health;

import com.trademesh.analytics.service.WarmupState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Custom Readiness check that returns DOWN until the semantic warm-up is complete.
 */
@Readiness
@ApplicationScoped
public class SemanticReadinessCheck implements HealthCheck {

    @Inject
    WarmupState warmupState;

    /**
     * Executes the readiness check.
     * @return UP if warm-up is complete, DOWN otherwise.
     */
    @Override
    public HealthCheckResponse call() {
        if (warmupState.isReady()) {
            return HealthCheckResponse.up("Analytics Service Warm-up Complete");
        } else {
            return HealthCheckResponse.down("Analytics Service Warming Up...");
        }
    }
}
