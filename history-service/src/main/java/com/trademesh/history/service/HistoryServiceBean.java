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

    /**
     * Consumes market price updates from RabbitMQ and archives them as transactions.
     * @param price The price event received from the message bus.
     */
    @Incoming("market-prices")
    @io.smallrye.common.annotation.Blocking
    public void archivePrice(io.vertx.core.json.JsonObject price) {
        String assetId = price.getString("assetId");
        double value = price.getDouble("value");
        LOG.debugf("Archiving price for %s: %.2f", assetId, value);
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
     * Retrieves historical OHLC data for charting.
     * Currently returns mocked data (Faza 3 placeholder).
     * @param request The history query request containing asset ID and time range.
     * @return A Uni emitting the history query response with OHLC series.
     */
    @Override
    @io.smallrye.common.annotation.Blocking
    public Uni<HistoryQueryResponse> getHistoricalData(HistoryQueryRequest request) {
        // Simple mock of OHLC data for now.
        // In real app, this would be a TimescaleDB continuous aggregate query.
        List<OHLCData> ohlcList = new ArrayList<>();
        long start = request.getStartTimestamp();
        long end = request.getEndTimestamp();
        long step = 60000; // 1 min

        for (long t = start; t < end; t += step) {
            ohlcList.add(OHLCData.newBuilder()
                .setTimestamp(t)
                .setOpen(100.0 + random.nextDouble() * 10.0)
                .setHigh(115.0)
                .setLow(95.0)
                .setClose(105.0)
                .setVolume(500.0)
                .build());
        }

        return Uni.createFrom().item(
            HistoryQueryResponse.newBuilder()
                .setAssetId(request.getAssetId())
                .addAllSeries(ohlcList)
                .build()
        );
    }
}
