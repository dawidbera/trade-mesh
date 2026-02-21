package com.trademesh.gateway.graphql.model;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Id;

@Name("Asset")
@Description("A financial asset with real-time price and analytics")
public class Asset {
    @Id
    public String id;
    public String symbol;
    public String name;
    
    // GraphQL resolvers will handle nested fields like currentPrice, analytics, etc.
}
