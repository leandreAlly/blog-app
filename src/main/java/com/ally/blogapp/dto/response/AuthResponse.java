package com.ally.blogapp.dto.response;

public record AuthResponse(
        String token,
        String username,
        String role,
        long expiresIn
) {}
