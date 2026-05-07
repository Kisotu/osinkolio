package com.musicstore.payment.api.v1.dto;

import com.musicstore.payment.domain.entity.PaymentMethod;
import com.musicstore.payment.domain.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        BigDecimal amount,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        String transactionId,
        String idempotencyKey,
        String failureReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Long version
) {}
