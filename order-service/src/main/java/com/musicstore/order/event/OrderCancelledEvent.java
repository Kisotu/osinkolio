package com.musicstore.order.event;

import java.time.Instant;

/**
 * Event emitted when an order is cancelled.
 */
public record OrderCancelledEvent(
        Long orderId,
        String reason,
        Instant timestamp
) implements SagaEvent {

    public OrderCancelledEvent(Long orderId, String reason) {
        this(orderId, reason, Instant.now());
    }
}
