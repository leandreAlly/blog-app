package com.ally.blogapp.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdatePostRequest(

        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        String content,

        List<String> tags
) {}
