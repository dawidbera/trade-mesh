package com.trademesh.gateway.graphql.model;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Description;
import java.time.Instant;

@Name("Price")
@Description("The current market price of an asset")
public class Price {
    public Double value;
    public Instant timestamp;
    public String currency;

    public Price() {}
    public Price(Double value, Instant timestamp, String currency) {
        this.value = value;
        this.timestamp = timestamp;
        this.currency = currency;
    }
}
