package com.ally.blogapp.service;

import com.ally.blogapp.exception.DuplicateResourceException;
import com.ally.blogapp.exception.ResourceNotFoundException;
import com.ally.blogapp.model.Post;
import com.ally.blogapp.model.Review;
import com.ally.blogapp.model.User;
import com.ally.blogapp.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final PostService postService;

    public ReviewService(ReviewRepository reviewRepository,
                         UserService userService,
                         PostService postService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.postService = postService;
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.REPEATABLE_READ,
                   rollbackFor = Exception.class)
    public Review create(Long postId, Long userId, int rating, String content) {
        if (reviewRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new DuplicateResourceException("User has already reviewed this post");
        }
        Post post = postService.findById(postId);
        User user = userService.findById(userId);
        return reviewRepository.save(new Review(post, user, rating, content));
    }

    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
    }

    public List<Review> findByPostId(Long postId) {
        return reviewRepository.findByPostId(postId);
    }

    public List<Review> findByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    public double getAverageRating(Long postId) {
        return reviewRepository.getAverageRatingByPostId(postId);
    }

    public Page<Review> findByPostId(Long postId, Pageable pageable) {
        return reviewRepository.findByPostId(postId, pageable);
    }

    public Page<Review> findByUserId(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable);
    }

    public Page<Review> findTopRated(Pageable pageable) {
        return reviewRepository.findTopRated(pageable);
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Review update(Long id, int rating, String content) {
        Review review = findById(id);
        review.setRating(rating);
        if (content != null) review.setContent(content);
        return reviewRepository.save(review);
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public void delete(Long id) {
        findById(id);
        reviewRepository.deleteById(id);
    }
}
