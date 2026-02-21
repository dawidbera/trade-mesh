package com.trademesh.analytics.logic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SmaCalculator.
 * Tests the mathematical logic in isolation without any infrastructure.
 */
public class SmaCalculatorTest {

    private final SmaCalculator calculator = new SmaCalculator();

    /**
     * Verifies basic average calculation for a standard list of numbers.
     */
    @Test
    @DisplayName("Should calculate average for a list of numbers")
    public void testAverage() {
        List<Double> prices = List.of(10.0, 20.0, 30.0);
        double result = calculator.calculate(prices);
        assertThat(result).isEqualTo(20.0);
    }

    /**
     * Ensures that an empty list results in a 0.0 average.
     */
    @Test
    @DisplayName("Should return 0.0 for an empty list")
    public void testEmptyList() {
        double result = calculator.calculate(Collections.emptyList());
        assertThat(result).isEqualTo(0.0);
    }

    /**
     * Ensures that null input is handled gracefully by returning 0.0.
     */
    @Test
    @DisplayName("Should return 0.0 for null input")
    public void testNullInput() {
        double result = calculator.calculate(null);
        assertThat(result).isEqualTo(0.0);
    }

    /**
     * Verifies calculation for a list containing only one element.
     */
    @Test
    @DisplayName("Should handle a single element list")
    public void testSingleElement() {
        double result = calculator.calculate(List.of(55.5));
        assertThat(result).isEqualTo(55.5);
    }
}
