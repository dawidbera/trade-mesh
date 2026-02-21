package com.trademesh.history.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity representing a historical transaction record.
 * Stored in PostgreSQL/TimescaleDB.
 */
@Entity
@Table(name = "transactions")
public class Transaction {
    /**
     * Unique database-generated identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /**
     * Unique identifier for the asset (e.g., BTC).
     */
    @Column(nullable = false)
    public String assetId;

    /**
     * The execution price of the transaction.
     */
    @Column(nullable = false)
    public Double price;

    /**
     * The quantity/volume of the asset traded.
     */
    @Column(nullable = false)
    public Double volume;

    /**
     * Precision timestamp of the transaction.
     */
    @Column(nullable = false)
    public Instant timestamp;

    /**
     * The side of the trade: "BUY" or "SELL".
     */
    @Column(nullable = false)
    public String side; // "BUY" or "SELL"
}
