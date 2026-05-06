package com.musicstore.order.api.v1.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CartResponse(
        Long id,
        Long userId,
        String sessionId,
        List<CartItemResponse> items,
        BigDecimal total,
        Integer totalItems,
        OffsetDateTime expiresAt
) {}
