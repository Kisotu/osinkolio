package com.musicstore.product.api.v1.dto;

import java.time.OffsetDateTime;
import java.util.Set;

public record CategoryResponse(
    Long id,
    String name,
    Long parentId,
    String parentName,
    Set<CategoryResponse> subcategories,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}