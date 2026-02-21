package com.trademesh.analytics.logic;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Utility for calculating Simple Moving Average (SMA).
 */
@ApplicationScoped
public class SmaCalculator {

    /**
     * Calculates the average of a list of prices.
     * @param prices The list of historical prices.
     * @return The average value, or 0.0 if the list is empty.
     */
    public double calculate(List<Double> prices) {
        if (prices == null || prices.isEmpty()) {
            return 0.0;
        }
        return prices.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}
