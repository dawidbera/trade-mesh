package com.trademesh.history.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shared state to track if the service has completed its semantic warm-up sequence.
 */
@ApplicationScoped
public class WarmupState {
    private final AtomicBoolean ready = new AtomicBoolean(false);

    /**
     * Marks the semantic warm-up sequence as successfully completed.
     */
    public void setReady() {
        ready.set(true);
    }

    /**
     * Checks whether the service has finished its warm-up tasks.
     * @return true if the service is ready to accept traffic.
     */
    public boolean isReady() {
        return ready.get();
    }
}
