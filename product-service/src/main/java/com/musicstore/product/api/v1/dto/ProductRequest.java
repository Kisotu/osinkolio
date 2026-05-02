package com.musicstore.product.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank(message = "SKU is required")
    String sku,
    @NotBlank(message = "Product name is required")
    String name,
    String description,
    Long categoryId,
    String brand,
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    BigDecimal price,
    @Positive(message = "Weight must be positive")
    BigDecimal weight,
    String images,
    Boolean isActive
) {}