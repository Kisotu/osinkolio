package com.musicstore.user.api.v1.dto;

import com.musicstore.user.domain.entity.Role;

public record AuthResponse(
        Long userId,
        String email,
        String firstName,
        String lastName,
        Role role,
        String accessToken,
        String refreshToken,
        long expiresIn
) {}
