package com.ally.blogapp.model;

import java.time.LocalDateTime;

public class PostStats {
    private final Long postId;
    private final String title;
    private final PostStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime publishedAt;
    private final int commentCount;
    private final int reviewCount;
    private final double avgRating;

    public PostStats(Long postId, String title, PostStatus status,
                     LocalDateTime createdAt, LocalDateTime publishedAt,
                     int commentCount, int reviewCount, double avgRating) {
        this.postId = postId;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
        this.commentCount = commentCount;
        this.reviewCount = reviewCount;
        this.avgRating = avgRating;
    }

    public Long getPostId()           { return postId; }
    public String getTitle()          { return title; }
    public PostStatus getStatus()     { return status; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public int getCommentCount()      { return commentCount; }
    public int getReviewCount()       { return reviewCount; }
    public double getAvgRating()      { return avgRating; }
}
