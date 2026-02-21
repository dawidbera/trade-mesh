package com.trademesh.market.service;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import com.trademesh.market.model.MarketPrice;

/**
 * Simulates market price movements for a set of assets.
 * Periodically updates asset prices stored in Redis to mimic a live market.
 * Publishes price updates to RabbitMQ via the 'market-prices' channel.
 */
@ApplicationScoped
public class MarketSimulator {

    private static final Logger LOG = Logger.getLogger(MarketSimulator.class);
    private final ValueCommands<String, Double> priceCommands;
    private final Random random = new Random();
    private final List<String> assets = List.of("BTC", "ETH", "AAPL", "GOOG", "TSLA");

    @Inject
    @Channel("market-prices")
    Emitter<MarketPrice> priceEmitter;

    /**
     * Initializes the simulator with a Redis data source.
     * @param ds The Redis data source.
     */
    @Inject
    public MarketSimulator(RedisDataSource ds) {
        this.priceCommands = ds.value(Double.class);
    }

    /**
     * Executed when the application starts. 
     * Initializes base asset prices if not present and starts the simulation ticker.
     * @param ev Startup event.
     */
    void onStart(@Observes StartupEvent ev) {
        LOG.info("MarketSimulator started, initializing asset prices...");
        assets.forEach(assetId -> {
            if (priceCommands.get("price:" + assetId) == null) {
                priceCommands.set("price:" + assetId, 100.0 + random.nextDouble() * 50.0);
            }
        });

        Multi.createFrom().ticks().every(Duration.ofSeconds(2))
            .subscribe().with(tick -> updatePrices());
    }

    /**
     * Updates prices for all tracked assets, stores in Redis and publishes to RabbitMQ.
     */
    private void updatePrices() {
        assets.forEach(assetId -> {
            Double currentPrice = priceCommands.get("price:" + assetId);
            if (currentPrice != null) {
                double change = (random.nextDouble() - 0.5) * 2.0; // +/- 1.0
                double newPrice = currentPrice + change;
                priceCommands.set("price:" + assetId, newPrice);
                
                // Publish to RabbitMQ
                priceEmitter.send(new MarketPrice(assetId, newPrice));
                
                LOG.debugf("Asset %s price updated to %.2f and published", assetId, newPrice);
            }
        });
    }
}
