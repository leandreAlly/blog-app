package com.ally.blogapp.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio,

        @Size(max = 255, message = "Profile image URL must not exceed 255 characters")
        String profileImage
) {}
