package com.musicstore.user.security;

import java.security.Principal;

public record UserPrincipal(Long userId, String email, String role) implements Principal {

    @Override
    public String getName() {
        return email;
    }
}
