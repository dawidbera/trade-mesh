package com.trademesh.gateway.graphql.model;

import org.eclipse.microprofile.graphql.Name;
import java.time.Instant;

/**
 * GraphQL model for a historical transaction.
 */
@Name("Transaction")
public class Transaction {
    /**
     * Execution price of the transaction.
     */
    public Double price;

    /**
     * Quantity of the asset traded.
     */
    public Double volume;

    /**
     * Timestamp of execution.
     */
    public Instant timestamp;

    /**
     * Trade side ("BUY" or "SELL").
     */
    public String side;

    /**
     * Default constructor for GraphQL.
     */
    public Transaction() {}

    /**
     * Full constructor for data mapping.
     * @param price Execution price.
     * @param volume Trade quantity.
     * @param timestamp Execution time.
     * @param side Trade side.
     */
    public Transaction(Double price, Double volume, Instant timestamp, String side) {
        this.price = price;
        this.volume = volume;
        this.timestamp = timestamp;
        this.side = side;
    }
}
