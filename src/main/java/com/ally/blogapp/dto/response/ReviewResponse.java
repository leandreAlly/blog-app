package com.ally.blogapp.dto.response;

import com.ally.blogapp.model.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long postId,
        Long userId,
        String username,
        int rating,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getPost().getId(),
                review.getUser().getId(),
                review.getUser().getUsername(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
