package com.trademesh.gateway.graphql;

import com.trademesh.gateway.graphql.model.Asset;
import com.trademesh.gateway.graphql.model.Price;
import com.trademesh.market.grpc.PriceRequest;
import com.trademesh.market.grpc.PriceService;
import com.trademesh.market.model.MarketPrice;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.security.Authenticated;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.reactive.messaging.Channel;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * GraphQL API for retrieving asset information.
 * Aggregates data from market-data-service, analytics-service, and history-service using gRPC.
 * Supports real-time price updates via GraphQL Subscriptions.
 * Implements Fault Tolerance via Circuit Breakers and Fallbacks.
 * Secured via OIDC (Keycloak).
 */
@GraphQLApi
@ApplicationScoped
@Authenticated
public class AssetApi {

    @GrpcClient("market")
    PriceService marketService;

    @GrpcClient("analytics")
    com.trademesh.analytics.grpc.AnalyticsService analyticsService;

    @GrpcClient("history")
    com.trademesh.history.grpc.HistoryService historyService;

    @Inject
    @Channel("market-prices")
    Multi<MarketPrice> priceStream;

    /**
     * GraphQL Query to get a specific asset by its ID.
     * @param id The asset ID (e.g., BTC, AAPL).
     * @return The Asset model.
     */
    @Query("asset")
    @Description("Get a specific asset by ID")
    public Asset getAsset(@Name("id") String id) {
        Asset asset = new Asset();
        asset.id = id;
        asset.symbol = id;
        asset.name = "Asset " + id;
        return asset;
    }

    /**
     * GraphQL Query to get all available assets.
     * @return A list of assets.
     */
    @Query("allAssets")
    @Description("Get all available assets")
    public List<Asset> getAllAssets() {
        return List.of(getAsset("BTC"), getAsset("ETH"), getAsset("AAPL"));
    }

    /**
     * Nested GraphQL resolver for the Asset.currentPrice field.
     * Fetches the latest price from the market-data-service via gRPC.
     * Includes a CircuitBreaker to handle potential service outages.
     * @param asset The parent Asset source.
     * @return A Uni emitting the current price.
     */
    @CircuitBreaker(requestVolumeThreshold = 4)
    @Fallback(fallbackMethod = "fallbackPrice")
    public Uni<Price> getCurrentPrice(@Source Asset asset) {
        return marketService.getPrice(PriceRequest.newBuilder().setAssetId(asset.id).build())
            .onItem().transform(resp -> new Price(resp.getValue(), Instant.ofEpochMilli(resp.getTimestamp()), resp.getCurrency()));
    }

    /**
     * Fallback method for getCurrentPrice. Returns a null price instead of failing.
     * @param asset The parent Asset source.
     * @return A Uni emitting null.
     */
    public Uni<Price> fallbackPrice(Asset asset) {
        return Uni.createFrom().nullItem();
    }

    /**
     * GraphQL Subscription for real-time price updates.
     * Consumes price events from RabbitMQ and filters them by assetId.
     * @param assetId The asset ID to subscribe to.
     * @return A Multi emitting price updates.
     */
    @Subscription
    @Description("Subscribe to real-time price updates for a specific asset")
    public Multi<Price> priceUpdates(@Name("assetId") String assetId) {
        return priceStream
            .filter(mp -> mp.assetId.equals(assetId))
            .map(mp -> new Price(mp.value, Instant.ofEpochMilli(mp.timestamp), mp.currency));
    }

    /**
     * Nested GraphQL resolver for the Asset.analytics field.
     * Fetches technical indicators from the analytics-service via gRPC.
     * If the service is down, fallbacks to an empty list.
     * @param asset The parent Asset source.
     * @return A Uni emitting a list of indicators.
     */
    @CircuitBreaker(requestVolumeThreshold = 4)
    @Fallback(fallbackMethod = "fallbackAnalytics")
    public Uni<List<com.trademesh.gateway.graphql.model.Indicator>> getAnalytics(@Source Asset asset) {
        return analyticsService.getIndicator(com.trademesh.analytics.grpc.IndicatorRequest.newBuilder()
                .setAssetId(asset.id).setIndicatorType("SMA").setPeriod(14).build())
            .onItem().transform(resp -> List.of(new com.trademesh.gateway.graphql.model.Indicator(
                resp.getIndicatorType(), resp.getValue(), Instant.ofEpochMilli(resp.getTimestamp()), resp.getMetadataMap())));
    }

    /**
     * Fallback method for getAnalytics. Returns an empty list.
     * @param asset The parent Asset source.
     * @return A Uni emitting an empty list.
     */
    public Uni<List<com.trademesh.gateway.graphql.model.Indicator>> fallbackAnalytics(Asset asset) {
        return Uni.createFrom().item(Collections.emptyList());
    }

    /**
     * Nested GraphQL resolver for the Asset.history field.
     * Fetches historical transaction data from the history-service via gRPC.
     * If the service is down, fallbacks to an empty list.
     * @param asset The parent Asset source.
     * @param limit Maximum number of historical records to return.
     * @return A Uni emitting a list of transactions.
     */
    @CircuitBreaker(requestVolumeThreshold = 4)
    @Fallback(fallbackMethod = "fallbackHistory")
    public Uni<List<com.trademesh.gateway.graphql.model.Transaction>> getHistory(@Source Asset asset, @Name("limit") Integer limit) {
        return historyService.getHistoricalData(com.trademesh.history.grpc.HistoryQueryRequest.newBuilder()
                .setAssetId(asset.id)
                .setStartTimestamp(Instant.now().minusSeconds(3600).toEpochMilli())
                .setEndTimestamp(Instant.now().toEpochMilli())
                .build())
            .onItem().transform(resp -> resp.getSeriesList().stream()
                .limit(limit != null ? limit : 10)
                .map(ohlc -> new com.trademesh.gateway.graphql.model.Transaction(
                    ohlc.getClose(), ohlc.getVolume(), Instant.ofEpochMilli(ohlc.getTimestamp()), "BUY"))
                .toList());
    }

    /**
     * Fallback method for getHistory. Returns an empty list.
     * @param asset The parent Asset source.
     * @param limit The limit parameter.
     * @return A Uni emitting an empty list.
     */
    public Uni<List<com.trademesh.gateway.graphql.model.Transaction>> fallbackHistory(Asset asset, Integer limit) {
        return Uni.createFrom().item(Collections.emptyList());
    }
}
