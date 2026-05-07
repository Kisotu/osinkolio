package com.musicstore.payment.api.v1.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RefundRequest(
        @NotNull(message = "Payment ID is required")
        Long paymentId,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {}
