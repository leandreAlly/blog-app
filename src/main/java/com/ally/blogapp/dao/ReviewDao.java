package com.ally.blogapp.dao;

import com.ally.blogapp.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewDao {
    Review save(Review review);
    Optional<Review> findById(Long id);
    List<Review> findByPostId(Long postId);
    List<Review> findByUserId(Long userId);
    Optional<Review> findByPostIdAndUserId(Long postId, Long userId);
    double getAverageRatingByPostId(Long postId);
    Review update(Review review);
    void delete(Long id);
}
