package com.musicstore.order.event;

import java.time.Instant;

/**
 * Event emitted when inventory is reserved for an order.
 */
public record InventoryReservedEvent(
        Long orderId,
        Long productId,
        Integer quantity,
        Boolean reserved,
        Instant timestamp
) implements SagaEvent {

    public InventoryReservedEvent(Long orderId, Long productId, Integer quantity, Boolean reserved) {
        this(orderId, productId, quantity, reserved, Instant.now());
    }
}
