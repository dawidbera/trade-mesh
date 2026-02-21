package com.trademesh.history.health;

import com.trademesh.history.service.WarmupState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Custom Readiness check for History Service.
 */
@Readiness
@ApplicationScoped
public class SemanticReadinessCheck implements HealthCheck {

    @Inject
    WarmupState warmupState;

    /**
     * Executes the readiness check. Returns UP only if the warm-up sequence is finished.
     * @return HealthCheckResponse indicating readiness.
     */
    @Override
    public HealthCheckResponse call() {
        if (warmupState.isReady()) {
            return HealthCheckResponse.up("History Service Warm-up Complete");
        } else {
            return HealthCheckResponse.down("History Service Warming Up...");
        }
    }
}
