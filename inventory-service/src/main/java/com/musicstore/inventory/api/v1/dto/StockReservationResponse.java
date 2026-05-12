package com.musicstore.inventory.api.v1.dto;

import java.time.Instant;

public record StockReservationResponse(
    Long id,
    Long productId,
    Long orderId,
    Integer quantity,
    String status,
    Instant createdAt
) {}
