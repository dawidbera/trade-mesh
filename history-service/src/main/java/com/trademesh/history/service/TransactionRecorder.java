package com.trademesh.history.service;

import com.trademesh.history.logic.TransactionFactory;
import com.trademesh.history.model.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Component responsible for persisting transaction records to the database.
 * Uses JPA for data access and manages transactions.
 */
@ApplicationScoped
public class TransactionRecorder {

    @Inject
    EntityManager em;

    @Inject
    TransactionFactory factory;

    /**
     * Persists a price update as a transaction record.
     * Uses the TransactionFactory to generate the entity.
     * @param assetId The ID of the asset.
     * @param price The price at the time of transaction.
     */
    @Transactional
    public void recordPriceAsTransaction(String assetId, double price) {
        Transaction tx = factory.create(assetId, price);
        em.persist(tx);
    }
}
