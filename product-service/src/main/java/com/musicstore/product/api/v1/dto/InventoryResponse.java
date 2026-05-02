package com.musicstore.product.api.v1.dto;

import java.time.OffsetDateTime;

public record InventoryResponse(
    Long productId,
    Integer quantityReservable,
    Integer quantityOnHand,
    Integer lowStockThreshold,
    boolean lowStock,
    OffsetDateTime updatedAt
) {}