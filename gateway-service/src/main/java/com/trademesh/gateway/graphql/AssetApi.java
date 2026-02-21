package com.trademesh.gateway.graphql;

import com.trademesh.gateway.graphql.model.Asset;
import com.trademesh.gateway.graphql.model.Price;
import com.trademesh.market.grpc.PriceRequest;
import com.trademesh.market.grpc.PriceService;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.graphql.*;
import java.time.Instant;
import java.util.List;

/**
 * GraphQL API for retrieving asset information.
 * Aggregates data from market-data-service, analytics-service, and history-service using gRPC.
 */
@GraphQLApi
@ApplicationScoped
public class AssetApi {

    @GrpcClient("market")
    PriceService marketService;

    @GrpcClient("analytics")
    com.trademesh.analytics.grpc.AnalyticsService analyticsService;

    @GrpcClient("history")
    com.trademesh.history.grpc.HistoryService historyService;

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
     * @param asset The parent Asset source.
     * @return A Uni emitting the current price.
     */
    public Uni<Price> getCurrentPrice(@Source Asset asset) {
        return marketService.getPrice(PriceRequest.newBuilder().setAssetId(asset.id).build())
            .onItem().transform(resp -> new Price(resp.getValue(), Instant.ofEpochMilli(resp.getTimestamp()), resp.getCurrency()));
    }

    /**
     * Nested GraphQL resolver for the Asset.analytics field.
     * Fetches technical indicators from the analytics-service via gRPC.
     * @param asset The parent Asset source.
     * @return A Uni emitting a list of indicators.
     */
    public Uni<List<com.trademesh.gateway.graphql.model.Indicator>> getAnalytics(@Source Asset asset) {
        return analyticsService.getIndicator(com.trademesh.analytics.grpc.IndicatorRequest.newBuilder()
                .setAssetId(asset.id).setIndicatorType("SMA").setPeriod(14).build())
            .onItem().transform(resp -> List.of(new com.trademesh.gateway.graphql.model.Indicator(
                resp.getIndicatorType(), resp.getValue(), Instant.ofEpochMilli(resp.getTimestamp()), resp.getMetadataMap())));
    }

    /**
     * Nested GraphQL resolver for the Asset.history field.
     * Fetches historical transaction data from the history-service via gRPC.
     * @param asset The parent Asset source.
     * @param limit Maximum number of historical records to return.
     * @return A Uni emitting a list of transactions.
     */
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
}
