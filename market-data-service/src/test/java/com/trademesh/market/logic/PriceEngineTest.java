package com.trademesh.market.logic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PriceEngine.
 */
public class PriceEngineTest {

    private final PriceEngine engine = new PriceEngine();

    /**
     * Verifies that the initial price is generated within the strictly 
     * defined business range (100-150).
     */
    @Test
    @DisplayName("Should generate initial price within expected range")
    public void testInitialPrice() {
        double price = engine.initialPrice();
        assertThat(price).isBetween(100.0, 150.0);
    }

    /**
     * Tests the random walk logic to ensure that the price drift stays 
     * within the +/- 1.0 limit.
     */
    @Test
    @DisplayName("Should update price within reasonable drift range")
    public void testNextPrice() {
        double currentPrice = 100.0;
        double nextPrice = engine.nextPrice(currentPrice);
        
        // Drift should be +/- 1.0
        assertThat(nextPrice).isBetween(99.0, 101.0);
        assertThat(nextPrice).isNotEqualTo(currentPrice);
    }
}
