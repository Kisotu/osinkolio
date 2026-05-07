package com.musicstore.order.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event emitted when a payment is successfully completed.
 */
public record PaymentCompletedEvent(
        Long paymentId,
        Long orderId,
        String transactionId,
        BigDecimal amount,
        Instant timestamp
) implements SagaEvent {

    public PaymentCompletedEvent(Long paymentId, Long orderId, String transactionId, BigDecimal amount) {
        this(paymentId, orderId, transactionId, amount, Instant.now());
    }
}
