package com.musicstore.user.api.v1.dto;

import com.musicstore.user.domain.entity.Role;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        Role role,
        Boolean isEnabled,
        Boolean isEmailVerified,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
