package com.trademesh.gateway.graphql.model;

import org.eclipse.microprofile.graphql.Name;
import java.time.Instant;

@Name("Transaction")
public class Transaction {
    public Double price;
    public Double volume;
    public Instant timestamp;
    public String side;

    public Transaction() {}
    public Transaction(Double price, Double volume, Instant timestamp, String side) {
        this.price = price;
        this.volume = volume;
        this.timestamp = timestamp;
        this.side = side;
    }
}
