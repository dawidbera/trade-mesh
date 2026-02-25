package com.trademesh.gateway.graphql.model;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Description;
import java.time.Instant;

/**
 * GraphQL model representing the current market price of an asset.
 */
@Name("Price")
@Description("The current market price of an asset")
public class Price {
    /**
     * The ID of the asset.
     */
    public String assetId;

    /**
     * The monetary value of the asset.
     */
    public Double value;

    /**
     * Precision timestamp of the price quote.
     */
    public Instant timestamp;

    /**
     * The currency code (e.g., "USD").
     */
    public String currency;

    /**
     * Default constructor for GraphQL.
     */
    public Price() {}

    /**
     * Full constructor for data mapping.
     * @param assetId The asset ID.
     * @param value Price value.
     * @param timestamp Time of quote.
     * @param currency Currency code.
     */
    public Price(String assetId, Double value, Instant timestamp, String currency) {
        this.assetId = assetId;
        this.value = value;
        this.timestamp = timestamp;
        this.currency = currency;
    }
}
