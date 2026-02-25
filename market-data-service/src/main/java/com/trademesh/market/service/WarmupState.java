package com.trademesh.market.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Global state indicating if the service has completed its semantic warm-up.
 */
@ApplicationScoped
public class WarmupState {
    private final AtomicBoolean ready = new AtomicBoolean(false);

    /**
     * Marks the service as ready.
     */
    public void setReady() {
        this.ready.set(true);
    }

    /**
     * Checks if the service is ready.
     * @return True if ready, false otherwise.
     */
    public boolean isReady() {
        return this.ready.get();
    }
}
