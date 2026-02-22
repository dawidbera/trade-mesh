package com.trademesh.analytics.service;

import com.trademesh.analytics.logic.SmaCalculator;
import com.trademesh.analytics.grpc.AnalyticsService;
import com.trademesh.analytics.grpc.IndicatorRequest;
import com.trademesh.analytics.grpc.IndicatorResponse;
import io.quarkus.grpc.GrpcService;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.list.ListCommands;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import com.trademesh.market.model.MarketPrice;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Implementation of the gRPC AnalyticsService.
 * Calculates technical indicators for assets based on price history stored in Redis.
 * Consumes live price updates from RabbitMQ via the 'market-prices' channel.
 */
@GrpcService
public class AnalyticsServiceBean implements AnalyticsService {

    private static final Logger LOG = Logger.getLogger(AnalyticsServiceBean.class);
    private final ListCommands<String, Double> priceLists;

    @Inject
    SmaCalculator smaCalculator;

    /**
     * Initializes the service with a Redis data source.
     * @param ds The Redis data source.
     */
    @Inject
    public AnalyticsServiceBean(RedisDataSource ds) {
        this.priceLists = ds.list(Double.class);
    }

    /**
     * Consumes market price updates from RabbitMQ.
     * Updates internal Redis price history for the specific asset.
     * @param price The price event received from the message bus.
     */
    @Incoming("market-prices")
    @io.smallrye.common.annotation.Blocking
    public void consumePrice(io.vertx.core.json.JsonObject price) {
        String assetId = price.getString("assetId");
        double value = price.getDouble("value");
        LOG.debugf("Received price for %s: %.2f", assetId, value);
        priceLists.lpush("history:" + assetId, value);
        priceLists.ltrim("history:" + assetId, 0, 99); // Keep last 100 prices
    }

    /**
     * Calculates a technical indicator for an asset.
     * Currently supports SMA (Simple Moving Average).
     * @param request The indicator request containing asset ID, type, and period.
     * @return A Uni emitting the indicator response.
     */
    @Override
    @io.smallrye.common.annotation.Blocking
    public Uni<IndicatorResponse> getIndicator(IndicatorRequest request) {
        String assetId = request.getAssetId();
        String type = request.getIndicatorType();
        int period = request.getPeriod();

        List<Double> prices = priceLists.lrange("history:" + assetId, 0, period - 1);
        double value = 0.0;
        
        if (type.equalsIgnoreCase("SMA")) {
            value = smaCalculator.calculate(prices);
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
    @io.smallrye.common.annotation.Blocking
    public Multi<IndicatorResponse> streamIndicators(IndicatorRequest request) {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(2))
            .onItem().transformToUniAndConcatenate(tick -> getIndicator(request));
    }
}
