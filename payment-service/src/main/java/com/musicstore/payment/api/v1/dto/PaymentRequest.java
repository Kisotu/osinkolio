package com.musicstore.payment.api.v1.dto;

import com.musicstore.payment.domain.entity.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @Size(max = 255, message = "Idempotency key must not exceed 255 characters")
        String idempotencyKey
) {}
