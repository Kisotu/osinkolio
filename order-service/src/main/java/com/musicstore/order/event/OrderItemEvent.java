package com.musicstore.order.event;

import java.math.BigDecimal;

/**
 * Represents an item within an order event.
 */
public record OrderItemEvent(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice
) {
}
