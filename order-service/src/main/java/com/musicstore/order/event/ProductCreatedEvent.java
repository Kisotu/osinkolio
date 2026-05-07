package com.musicstore.order.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event emitted when a new product is created in the catalog.
 */
public record ProductCreatedEvent(
        Long productId,
        String sku,
        String name,
        BigDecimal price,
        String category,
        String brand,
        Instant timestamp
) implements SagaEvent {

    public ProductCreatedEvent(Long productId, String sku, String name, BigDecimal price, String category, String brand) {
        this(productId, sku, name, price, category, brand, Instant.now());
    }
}
