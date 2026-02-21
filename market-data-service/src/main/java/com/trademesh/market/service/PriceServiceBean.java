package com.trademesh.market.service;

import com.trademesh.market.grpc.PriceRequest;
import com.trademesh.market.grpc.PriceResponse;
import com.trademesh.market.grpc.PriceService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

/**
 * Implementation of the gRPC PriceService.
 * Provides real-time and streamed market prices for assets, backed by Redis.
 */
@GrpcService
public class PriceServiceBean implements PriceService {

    private final ValueCommands<String, Double> priceCommands;
    private final Random random = new Random();

    /**
     * Initializes the service with a Redis data source.
     * @param ds The Redis data source.
     */
    @Inject
    public PriceServiceBean(RedisDataSource ds) {
        this.priceCommands = ds.value(Double.class);
    }

    /**
     * Retrieves the current price for a specific asset.
     * If the price is not present in Redis, a random initial price is generated and stored.
     * @param request The price request containing the asset ID.
     * @return A Uni emitting the price response.
     */
    @Override
    public Uni<PriceResponse> getPrice(PriceRequest request) {
        String assetId = request.getAssetId();
        Double currentPrice = priceCommands.get("price:" + assetId);
        
        if (currentPrice == null) {
            // Initial price if not in Redis
            currentPrice = 100.0 + random.nextDouble() * 50.0;
            priceCommands.set("price:" + assetId, currentPrice);
        }

        return Uni.createFrom().item(
            PriceResponse.newBuilder()
                .setAssetId(assetId)
                .setValue(currentPrice)
                .setTimestamp(Instant.now().toEpochMilli())
                .setCurrency("USD")
                .build()
        );
    }

    /**
     * Streams real-time price updates for a specific asset.
     * Updates are sent every second.
     * @param request The price request containing the asset ID.
     * @return A Multi emitting a stream of price responses.
     */
    @Override
    public Multi<PriceResponse> streamPrices(PriceRequest request) {
        String assetId = request.getAssetId();
        
        return Multi.createFrom().ticks().every(Duration.ofSeconds(1))
            .onItem().transformToUniAndConcatenate(tick -> getPrice(request));
    }
}
