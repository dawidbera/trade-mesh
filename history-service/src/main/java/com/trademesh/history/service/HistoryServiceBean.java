package com.trademesh.history.service;

import com.trademesh.history.grpc.*;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import com.trademesh.market.model.MarketPrice;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Implementation of the gRPC HistoryService.
 * Archives transaction and price history in a persistent database.
 * Consumes live price updates from RabbitMQ via the 'market-prices' channel to record history.
 */
@GrpcService
public class HistoryServiceBean implements HistoryService {

    private static final Logger LOG = Logger.getLogger(HistoryServiceBean.class);
    private final Random random = new Random();

    @Inject
    TransactionRecorder recorder;

    @Inject
    jakarta.persistence.EntityManager em;

    /**
     * Consumes market price updates from RabbitMQ and archives them as transactions.
     * @param price The price event received from the message bus.
     */
    @Incoming("market-prices")
    @io.smallrye.common.annotation.Blocking
    public void archivePrice(io.vertx.core.json.JsonObject price) {
        String assetId = price.getString("assetId");
        double value = price.getDouble("value");
        LOG.infof("Archiving price for %s: %.2f", assetId, value);
        recorder.recordPriceAsTransaction(assetId, value);
    }

    /**
     * Manually records a single transaction request.
     * @param request The transaction request.
     * @return A Uni emitting the transaction response.
     */
    @Override
    @io.smallrye.common.annotation.Blocking
    public Uni<TransactionResponse> recordTransaction(TransactionRequest request) {
        recorder.recordPriceAsTransaction(request.getAssetId(), request.getPrice());

        return Uni.createFrom().item(
            TransactionResponse.newBuilder().setSuccess(true).setMessage("Transaction recorded").build()
        );
    }

    /**
     * Retrieves historical OHLC data for charting from TimescaleDB aggregates.
     * Falls back to empty list if the aggregate view is not available.
     * @param request The history query request containing asset ID and time range.
     * @return A Uni emitting the history query response with OHLC series.
     */
    @Override
    @io.smallrye.common.annotation.Blocking
    public Uni<HistoryQueryResponse> getHistoricalData(HistoryQueryRequest request) {
        List<OHLCData> ohlcList = new ArrayList<>();
        
        try {
            // Check if ohlc_1m view exists before querying
            Object viewExists = em.createNativeQuery(
                "SELECT EXISTS (SELECT FROM pg_matviews WHERE matviewname = 'ohlc_1m')")
                .getSingleResult();

            String query;
            if (Boolean.TRUE.equals(viewExists)) {
                // Use optimized TimescaleDB continuous aggregate
                query = "SELECT bucket, open, high, low, close, volume FROM ohlc_1m " +
                        "WHERE assetId = :assetId AND bucket >= :start AND bucket <= :end " +
                        "ORDER BY bucket ASC";
            } else {
                // Fallback to raw transactions for environments without TimescaleDB (like tests)
                LOG.warn("TimescaleDB OHLC view not found. Falling back to standard aggregation.");
                query = "SELECT date_trunc('minute', timestamp) as bucket, " +
                        "MIN(price) as open, MAX(price) as high, MIN(price) as low, MAX(price) as close, SUM(volume) as volume " +
                        "FROM transactions WHERE assetId = :assetId AND timestamp >= :start AND timestamp <= :end " +
                        "GROUP BY bucket ORDER BY bucket ASC";
            }

            @SuppressWarnings("unchecked")
            List<Object[]> results = em.createNativeQuery(query)
                .setParameter("assetId", request.getAssetId())
                .setParameter("start", Instant.ofEpochMilli(request.getStartTimestamp()))
                .setParameter("end", Instant.ofEpochMilli(request.getEndTimestamp()))
                .getResultList();

            for (Object[] row : results) {
                long ts;
                if (row[0] instanceof java.sql.Timestamp) {
                    ts = ((java.sql.Timestamp) row[0]).toInstant().toEpochMilli();
                } else if (row[0] instanceof java.time.Instant) {
                    ts = ((java.time.Instant) row[0]).toEpochMilli();
                } else if (row[0] instanceof java.time.OffsetDateTime) {
                    ts = ((java.time.OffsetDateTime) row[0]).toInstant().toEpochMilli();
                } else if (row[0] instanceof java.util.Date) {
                    ts = ((java.util.Date) row[0]).getTime();
                } else {
                    LOG.warnf("Unexpected timestamp type: %s", row[0].getClass().getName());
                    ts = Instant.now().toEpochMilli();
                }

                ohlcList.add(OHLCData.newBuilder()
                    .setTimestamp(ts)
                    .setOpen((Double) row[1])
                    .setHigh((Double) row[2])
                    .setLow((Double) row[3])
                    .setClose((Double) row[4])
                    .setVolume((Double) row[5])
                    .build());
            }
            LOG.infof("Retrieved %d OHLC records for %s", ohlcList.size(), request.getAssetId());
            
        } catch (Exception e) {
            LOG.error("Failed to fetch historical data", e);
        }

        return Uni.createFrom().item(
            HistoryQueryResponse.newBuilder()
                .setAssetId(request.getAssetId())
                .addAllSeries(ohlcList)
                .build()
        );
    }
}
