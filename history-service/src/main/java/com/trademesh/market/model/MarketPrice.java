package com.trademesh.market.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;

/**
 * Market price model used for archival in history-service.
 */
@RegisterForReflection
public class MarketPrice {
    public String assetId;
    public Double value;
    public long timestamp;
    public String currency;

    /**
     * Required default constructor.
     */
    public MarketPrice() {}

    /**
     * Initializes a price update object.
     * @param assetId Asset ID.
     * @param value Price value.
     */
    public MarketPrice(String assetId, Double value) {
        this.assetId = assetId;
        this.value = value;
        this.timestamp = Instant.now().toEpochMilli();
        this.currency = "USD";
    }
}
