package com.ally.blogapp.service;

import com.ally.blogapp.dao.ReviewDao;
import com.ally.blogapp.dao.impl.ReviewDaoImpl;
import com.ally.blogapp.model.Review;

import java.util.List;
import java.util.Optional;

public class ReviewService {

    private final ReviewDao reviewDao;

    public ReviewService() {
        this.reviewDao = new ReviewDaoImpl();
    }

    public Review create(Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (reviewDao.findByPostIdAndUserId(review.getPostId(), review.getUserId()).isPresent()) {
            throw new IllegalArgumentException("User has already reviewed this post");
        }
        return reviewDao.save(review);
    }

    public Optional<Review> findById(Long id) {
        return reviewDao.findById(id);
    }

    public List<Review> findByPostId(Long postId) {
        return reviewDao.findByPostId(postId);
    }

    public List<Review> findByUserId(Long userId) {
        return reviewDao.findByUserId(userId);
    }

    public double getAverageRating(Long postId) {
        return reviewDao.getAverageRatingByPostId(postId);
    }

    public Review update(Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        reviewDao.findById(review.getId())
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        return reviewDao.update(review);
    }

    public void delete(Long id) {
        reviewDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        reviewDao.delete(id);
    }
}
