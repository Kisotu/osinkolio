package com.musicstore.order.event;

import java.time.Instant;

/**
 * Event emitted when inventory reservation fails.
 */
public record InventoryFailedEvent(
        Long orderId,
        Long productId,
        String reason,
        Instant timestamp
) implements SagaEvent {

    public InventoryFailedEvent(Long orderId, Long productId, String reason) {
        this(orderId, productId, reason, Instant.now());
    }
}
