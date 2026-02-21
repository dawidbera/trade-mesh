package com.trademesh.history.logic;

import com.trademesh.history.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TransactionFactory.
 */
public class TransactionFactoryTest {

    private final TransactionFactory factory = new TransactionFactory();

    /**
     * Verifies that the factory correctly initializes a Transaction object 
     * with valid ranges and fields.
     */
    @Test
    @DisplayName("Should create a valid Transaction entity")
    public void testCreateTransaction() {
        String assetId = "ETH";
        double price = 2500.0;

        Transaction tx = factory.create(assetId, price);

        assertThat(tx).isNotNull();
        assertThat(tx.assetId).isEqualTo(assetId);
        assertThat(tx.price).isEqualTo(price);
        assertThat(tx.volume).isBetween(10.0, 110.0);
        assertThat(tx.timestamp).isNotNull();
        assertThat(tx.side).isIn("BUY", "SELL");
    }
}
