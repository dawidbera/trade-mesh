package com.trademesh.gateway.graphql.model;

import org.eclipse.microprofile.graphql.Name;
import java.time.Instant;

/**
 * GraphQL model for a single OHLC (Open-High-Low-Close) data point.
 */
@Name("OHLCPoint")
public class OHLCPoint {
    public Instant timestamp;
    public Double open;
    public Double high;
    public Double low;
    public Double close;
    public Double volume;

    public OHLCPoint() {}

    public OHLCPoint(Instant timestamp, Double open, Double high, Double low, Double close, Double volume) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
}
