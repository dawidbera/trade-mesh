package com.trademesh.gateway.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shared state to track if the service has completed its semantic warm-up sequence.
 */
@ApplicationScoped
public class WarmupState {
    private final AtomicBoolean ready = new AtomicBoolean(false);

    /**
     * Marks the warm-up as complete.
     */
    public void setReady() {
        ready.set(true);
    }

    /**
     * Checks if the warm-up is complete.
     * @return true if ready.
     */
    public boolean isReady() {
        return ready.get();
    }
}
