package com.trademesh.history.service;

import com.trademesh.history.model.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Random;

/**
 * Component responsible for persisting transaction records to the database.
 * Uses JPA for data access and manages transactions.
 */
@ApplicationScoped
public class TransactionRecorder {

    @Inject
    EntityManager em;

    private final Random random = new Random();

    /**
     * Persists a price update as a transaction record.
     * Generates a random volume and side (BUY/SELL) for the transaction.
     * @param assetId The ID of the asset.
     * @param price The price at the time of transaction.
     */
    @Transactional
    public void recordPriceAsTransaction(String assetId, double price) {
        Transaction tx = new Transaction();
        tx.assetId = assetId;
        tx.price = price;
        tx.volume = 10.0 + random.nextDouble() * 100.0;
        tx.timestamp = Instant.now();
        tx.side = random.nextBoolean() ? "BUY" : "SELL";
        em.persist(tx);
    }
}
