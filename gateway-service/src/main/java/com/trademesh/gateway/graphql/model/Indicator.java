package com.trademesh.gateway.graphql.model;

import org.eclipse.microprofile.graphql.Name;
import java.time.Instant;
import java.util.Map;

@Name("Indicator")
public class Indicator {
    public String type;
    public Double value;
    public Instant timestamp;
    public Map<String, String> metadata;

    public Indicator() {}
    public Indicator(String type, Double value, Instant timestamp, Map<String, String> metadata) {
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }
}
