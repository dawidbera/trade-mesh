package com.trademesh.market.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;

/**
 * Represents a price update for a financial asset.
 * This class is used for serializing price data sent over RabbitMQ.
 */
@RegisterForReflection
public class MarketPrice {
    public String assetId;
    public Double value;
    public long timestamp;
    public String currency;

    /**
     * Default constructor required for JSON deserialization.
     */
    public MarketPrice() {}

    /**
     * Creates a new market price update with the current timestamp.
     * @param assetId The unique identifier for the asset (e.g., "BTC").
     * @param value The current market price.
     */
    public MarketPrice(String assetId, Double value) {
        this.assetId = assetId;
        this.value = value;
        this.timestamp = Instant.now().toEpochMilli();
        this.currency = "USD";
    }
}
