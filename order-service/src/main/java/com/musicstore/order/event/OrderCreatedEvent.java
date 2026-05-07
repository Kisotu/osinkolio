package com.musicstore.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Event emitted when a new order is created.
 */
public record OrderCreatedEvent(
        Long orderId,
        String orderNumber,
        Long userId,
        List<OrderItemEvent> items,
        BigDecimal totalAmount,
        Instant timestamp
) implements SagaEvent {

    public OrderCreatedEvent(Long orderId, String orderNumber, Long userId, List<OrderItemEvent> items, BigDecimal totalAmount) {
        this(orderId, orderNumber, userId, items, totalAmount, Instant.now());
    }
}
