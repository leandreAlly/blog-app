package com.ally.blogapp.dto.response;

import com.ally.blogapp.model.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String role,
        String bio,
        String profileImage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getBio(),
                user.getProfileImage(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
