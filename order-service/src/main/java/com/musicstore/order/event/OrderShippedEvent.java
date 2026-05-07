package com.musicstore.order.event;

import java.time.Instant;

/**
 * Event emitted when an order is shipped.
 */
public record OrderShippedEvent(
        Long orderId,
        String trackingNumber,
        String carrier,
        Instant shippedAt,
        Instant timestamp
) implements SagaEvent {

    public OrderShippedEvent(Long orderId, String trackingNumber, String carrier, Instant shippedAt) {
        this(orderId, trackingNumber, carrier, shippedAt, Instant.now());
    }
}
