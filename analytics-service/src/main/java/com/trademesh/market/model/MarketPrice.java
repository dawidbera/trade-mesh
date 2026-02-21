package com.trademesh.market.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;

/**
 * Represents a price update for a financial asset, duplicated for consumption in analytics service.
 * Used for deserializing price data from RabbitMQ.
 */
@RegisterForReflection
public class MarketPrice {
    public String assetId;
    public Double value;
    public long timestamp;
    public String currency;

    /**
     * Default constructor for JSON deserialization.
     */
    public MarketPrice() {}

    /**
     * Creates a new market price update.
     * @param assetId The unique identifier for the asset.
     * @param value The current market price.
     */
    public MarketPrice(String assetId, Double value) {
        this.assetId = assetId;
        this.value = value;
        this.timestamp = Instant.now().toEpochMilli();
        this.currency = "USD";
    }
}
