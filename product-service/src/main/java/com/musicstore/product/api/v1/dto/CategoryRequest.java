package com.musicstore.product.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
    @NotBlank(message = "Category name is required")
    String name,
    Long parentId
) {}