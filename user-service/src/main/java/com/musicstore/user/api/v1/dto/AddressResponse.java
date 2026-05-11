package com.musicstore.user.api.v1.dto;

import java.time.OffsetDateTime;

public record AddressResponse(
        Long id,
        Long userId,
        String label,
        String street,
        String city,
        String state,
        String postalCode,
        String country,
        Boolean isDefault,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
