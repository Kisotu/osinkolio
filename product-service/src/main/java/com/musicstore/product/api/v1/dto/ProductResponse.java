package com.musicstore.product.api.v1.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductResponse(
    Long id,
    String sku,
    String name,
    String description,
    Long categoryId,
    String categoryName,
    String brand,
    BigDecimal price,
    BigDecimal weight,
    String images,
    Boolean isActive,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}