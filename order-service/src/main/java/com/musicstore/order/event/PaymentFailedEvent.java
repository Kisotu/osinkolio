package com.musicstore.order.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event emitted when a payment fails.
 */
public record PaymentFailedEvent(
        Long paymentId,
        Long orderId,
        String reason,
        BigDecimal amount,
        Instant timestamp
) implements SagaEvent {

    public PaymentFailedEvent(Long paymentId, Long orderId, String reason, BigDecimal amount) {
        this(paymentId, orderId, reason, amount, Instant.now());
    }
}
