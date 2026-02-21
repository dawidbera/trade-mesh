package com.trademesh.gateway.graphql.model;

import org.eclipse.microprofile.graphql.Name;
import java.time.Instant;
import java.util.Map;

/**
 * GraphQL model for a technical indicator (e.g., SMA, RSI).
 */
@Name("Indicator")
public class Indicator {
    /**
     * The type of indicator (e.g., "SMA").
     */
    public String type;

    /**
     * The calculated value of the indicator.
     */
    public Double value;

    /**
     * Timestamp of the calculation.
     */
    public Instant timestamp;

    /**
     * Additional metadata for the indicator calculation (e.g., period).
     */
    public Map<String, String> metadata;

    /**
     * Default constructor.
     */
    public Indicator() {}

    /**
     * Full constructor.
     * @param type Indicator type.
     * @param value Calculated value.
     * @param timestamp Calculation time.
     * @param metadata Supporting metadata.
     */
    public Indicator(String type, Double value, Instant timestamp, Map<String, String> metadata) {
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }
}
