package com.trademesh.history.logic;

import com.trademesh.history.model.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.Random;

/**
 * Factory for creating Transaction entities.
 */
@ApplicationScoped
public class TransactionFactory {

    private final Random random = new Random();

    /**
     * Creates a new Transaction object based on asset ID and price.
     * Randomly assigns volume and trade side.
     * @param assetId Asset ID.
     * @param price Execution price.
     * @return A populated Transaction entity.
     */
    public Transaction create(String assetId, double price) {
        Transaction tx = new Transaction();
        tx.assetId = assetId;
        tx.price = price;
        tx.volume = 10.0 + random.nextDouble() * 100.0;
        tx.timestamp = Instant.now();
        tx.side = random.nextBoolean() ? "BUY" : "SELL";
        return tx;
    }
}
