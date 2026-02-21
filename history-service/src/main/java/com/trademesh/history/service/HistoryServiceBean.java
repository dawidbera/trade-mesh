package com.trademesh.history.service;

import com.trademesh.history.grpc.*;
import com.trademesh.history.model.Transaction;
import com.trademesh.market.grpc.PriceRequest;
import com.trademesh.market.grpc.PriceService;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcService;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import org.jboss.logging.Logger;

/**
 * Implementation of the gRPC HistoryService.
 * Archives transaction and price history in a persistent database.
 * Subscribes to live price streams from market-data-service to record price changes.
 */
@GrpcService
public class HistoryServiceBean implements HistoryService {

    private static final Logger LOG = Logger.getLogger(HistoryServiceBean.class);
    private final List<String> assets = List.of("BTC", "ETH", "AAPL", "GOOG", "TSLA");
    private final Random random = new Random();

    @Inject
    TransactionRecorder recorder;

    @GrpcClient("market")
    PriceService marketService;

    /**
     * Executed when the application starts.
     * Initiates price archiving by subscribing to live price streams for all assets.
     * @param ev Startup event.
     */
    void onStart(@Observes StartupEvent ev) {
        LOG.info("HistoryService started, archiving price changes as transactions...");
        assets.forEach(this::archivePriceChanges);
    }

    /**
     * Subscribes to the price stream for a specific asset and records each update as a transaction.
     * @param assetId The ID of the asset to archive.
     */
    private void archivePriceChanges(String assetId) {
        marketService.streamPrices(PriceRequest.newBuilder().setAssetId(assetId).build())
            .subscribe().with(
                price -> recorder.recordPriceAsTransaction(price.getAssetId(), price.getValue()),
                failure -> LOG.errorf("Price archiving failed for %s: %s", assetId, failure.getMessage())
            );
    }

    /**
     * Manually records a single transaction request.
     * @param request The transaction request.
     * @return A Uni emitting the transaction response.
     */
    @Override
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
