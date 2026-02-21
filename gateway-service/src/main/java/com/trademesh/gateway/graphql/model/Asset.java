package com.trademesh.gateway.graphql.model;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Id;

/**
 * GraphQL model for a financial Asset.
 * Represents the root entity for asset-related queries.
 */
@Name("Asset")
@Description("A financial asset with real-time price and analytics")
public class Asset {
    /**
     * Unique identifier for the asset.
     */
    @Id
    public String id;

    /**
     * Trading symbol (e.g., BTC, AAPL).
     */
    public String symbol;

    /**
     * Display name of the asset.
     */
    public String name;
    
    // GraphQL resolvers will handle nested fields like currentPrice, analytics, etc.
}
