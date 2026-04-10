package com.ally.blogapp.dto.response;

import com.ally.blogapp.model.Tag;

import java.time.LocalDateTime;

public record TagResponse(Long id, String name, LocalDateTime createdAt) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getCreatedAt());
    }
}
