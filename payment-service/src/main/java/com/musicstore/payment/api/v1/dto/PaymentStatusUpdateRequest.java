package com.musicstore.payment.api.v1.dto;

import com.musicstore.payment.domain.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record PaymentStatusUpdateRequest(
        @NotNull(message = "Status is required")
        PaymentStatus status
) {}
