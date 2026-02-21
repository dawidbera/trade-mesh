package com.trademesh.history.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String assetId;

    @Column(nullable = false)
    public Double price;

    @Column(nullable = false)
    public Double volume;

    @Column(nullable = false)
    public Instant timestamp;

    @Column(nullable = false)
    public String side; // "BUY" or "SELL"
}
