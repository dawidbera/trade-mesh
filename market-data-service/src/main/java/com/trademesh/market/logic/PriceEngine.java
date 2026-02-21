package com.trademesh.market.logic;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Random;

/**
 * Logic for calculating market price movements.
 */
@ApplicationScoped
public class PriceEngine {

    private final Random random = new Random();

    /**
     * Calculates a new price based on the current price and a random walk.
     * @param currentPrice The current price of the asset.
     * @return The updated price.
     */
    public double nextPrice(double currentPrice) {
        double change = (random.nextDouble() - 0.5) * 2.0; // +/- 1.0
        return currentPrice + change;
    }

    /**
     * Generates an initial price for an asset.
     * @return A random initial price between 100 and 150.
     */
    public double initialPrice() {
        return 100.0 + random.nextDouble() * 50.0;
    }
}
