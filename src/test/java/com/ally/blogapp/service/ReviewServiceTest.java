package com.ally.blogapp.service;

import com.ally.blogapp.exception.DuplicateResourceException;
import com.ally.blogapp.exception.ResourceNotFoundException;
import com.ally.blogapp.model.*;
import com.ally.blogapp.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private UserService userService;
    @Mock private PostService postService;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void create_shouldSaveReview_whenNoDuplicate() {
        when(reviewRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(false);
        Post post = new Post(); post.setId(1L);
        User user = new User(); user.setId(2L);
        when(postService.findById(1L)).thenReturn(post);
        when(userService.findById(2L)).thenReturn(user);
        Review saved = new Review(post, user, 4, "Great!");
        saved.setId(10L);
        when(reviewRepository.save(any())).thenReturn(saved);

        Review result = reviewService.create(1L, 2L, 4, "Great!");

        assertThat(result.getRating()).isEqualTo(4);
    }

    @Test
    void create_shouldThrow_whenUserAlreadyReviewed() {
        when(reviewRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(1L, 2L, 3, "ok"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already reviewed");
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAverageRating_shouldReturnAverage() {
        when(reviewRepository.getAverageRatingByPostId(1L)).thenReturn(3.75);

        assertThat(reviewService.getAverageRating(1L)).isEqualTo(3.75);
    }
}
