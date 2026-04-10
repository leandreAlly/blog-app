package com.ally.blogapp.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequest(
        @NotBlank(message = "Content is required")
        String content
) {}
