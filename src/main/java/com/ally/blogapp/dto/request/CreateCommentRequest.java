package com.ally.blogapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(

        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank(message = "Content is required")
        String content
) {}
