package com.ally.blogapp.graphql;

import com.ally.blogapp.model.Review;
import com.ally.blogapp.service.ReviewService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ReviewResolver {

    private final ReviewService reviewService;

    public ReviewResolver(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @QueryMapping
    public Review review(@Argument Long id) {
        return reviewService.findById(id);
    }

    @QueryMapping
    public List<Review> reviewsByPost(@Argument Long postId) {
        return reviewService.findByPostId(postId);
    }

    @QueryMapping
    public Double averageRating(@Argument Long postId) {
        return reviewService.getAverageRating(postId);
    }

    @MutationMapping
    public Review createReview(@Argument Long postId, @Argument Long userId,
                               @Argument Integer rating, @Argument String content) {
        return reviewService.create(postId, userId, rating, content);
    }

    @MutationMapping
    public Review updateReview(@Argument Long id, @Argument Integer rating,
                               @Argument String content) {
        return reviewService.update(id, rating, content);
    }

    @MutationMapping
    public Boolean deleteReview(@Argument Long id) {
        reviewService.delete(id);
        return true;
    }
}
