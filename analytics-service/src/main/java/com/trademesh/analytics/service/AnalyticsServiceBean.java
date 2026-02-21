package com.trademesh.analytics.service;

import com.trademesh.analytics.grpc.AnalyticsService;
import com.trademesh.analytics.grpc.IndicatorRequest;
import com.trademesh.analytics.grpc.IndicatorResponse;
import com.trademesh.market.grpc.PriceRequest;
import com.trademesh.market.grpc.PriceService;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcService;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.list.ListCommands;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;

/**
 * Implementation of the gRPC AnalyticsService.
 * Calculates technical indicators for assets based on price history stored in Redis.
 * Subscribes to live price streams from market-data-service.
 */
@GrpcService
public class AnalyticsServiceBean implements AnalyticsService {

    private static final Logger LOG = Logger.getLogger(AnalyticsServiceBean.class);
    private final ListCommands<String, Double> priceLists;
    private final List<String> assets = List.of("BTC", "ETH", "AAPL", "GOOG", "TSLA");

    @GrpcClient("market")
    PriceService marketService;

    /**
     * Initializes the service with a Redis data source.
     * @param ds The Redis data source.
     */
    @Inject
    public AnalyticsServiceBean(RedisDataSource ds) {
        this.priceLists = ds.list(Double.class);
    }

    /**
     * Executed when the application starts.
     * Initiates subscriptions to live price streams for all supported assets.
     * @param ev Startup event.
     */
    void onStart(@Observes StartupEvent ev) {
        LOG.info("AnalyticsService started, subscribing to market price streams...");
        assets.forEach(this::subscribeToPriceStream);
    }

    /**
     * Subscribes to the price stream for a specific asset and stores updates in Redis.
     * Keeps only the last 100 prices for each asset.
     * @param assetId The ID of the asset to subscribe to.
     */
    private void subscribeToPriceStream(String assetId) {
        marketService.streamPrices(PriceRequest.newBuilder().setAssetId(assetId).build())
            .subscribe().with(
                price -> {
                    priceLists.lpush("history:" + assetId, price.getValue());
                    priceLists.ltrim("history:" + assetId, 0, 99); // Keep last 100 prices
                },
                failure -> LOG.errorf("Price stream failed for %s: %s", assetId, failure.getMessage())
            );
    }

    /**
     * Calculates a technical indicator for an asset.
     * Currently supports SMA (Simple Moving Average).
     * @param request The indicator request containing asset ID, type, and period.
     * @return A Uni emitting the indicator response.
     */
    @Override
    public Uni<IndicatorResponse> getIndicator(IndicatorRequest request) {
        String assetId = request.getAssetId();
        String type = request.getIndicatorType();
        int period = request.getPeriod();

        List<Double> prices = priceLists.lrange("history:" + assetId, 0, period - 1);
        double value = 0.0;
        
        if (type.equalsIgnoreCase("SMA") && !prices.isEmpty()) {
            value = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        } else {
            // Placeholder for other indicators
            value = Math.random() * 100.0;
        }

        return Uni.createFrom().item(
            IndicatorResponse.newBuilder()
                .setAssetId(assetId)
                .setIndicatorType(type)
                .setValue(value)
                .setTimestamp(Instant.now().toEpochMilli())
                .putMetadata("period", String.valueOf(period))
                .build()
        );
    }

    /**
     * Streams technical indicator updates for a specific asset.
     * Recalculates and sends the indicator every 2 seconds.
     * @param request The indicator request.
     * @return A Multi emitting a stream of indicator responses.
     */
    @Override
    public Multi<IndicatorResponse> streamIndicators(IndicatorRequest request) {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(2))
            .onItem().transformToUniAndConcatenate(tick -> getIndicator(request));
    }
}
