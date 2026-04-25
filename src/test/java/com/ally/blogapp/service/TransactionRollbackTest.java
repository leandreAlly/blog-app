package com.ally.blogapp.service;

import com.ally.blogapp.exception.DuplicateResourceException;
import com.ally.blogapp.exception.ResourceNotFoundException;
import com.ally.blogapp.model.Post;
import com.ally.blogapp.model.Review;
import com.ally.blogapp.model.User;
import com.ally.blogapp.repository.CommentRepository;
import com.ally.blogapp.repository.PostRepository;
import com.ally.blogapp.repository.ReviewRepository;
import com.ally.blogapp.repository.TagRepository;
import com.ally.blogapp.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TransactionRollbackTest {

    @Autowired private UserService userService;
    @Autowired private PostService postService;
    @Autowired private ReviewService reviewService;

    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private TagRepository tagRepository;

    @AfterEach
    void cleanup() {
        reviewRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    void register_duplicateUsername_rollsBackWithoutCreatingUser() {
        userService.register("rollback_user_a", "rollback_a@test.local", "pw", "READER");
        long countBefore = userRepository.count();

        assertThatThrownBy(() ->
                userService.register("rollback_user_a", "rollback_a2@test.local", "pw", "READER"))
                .isInstanceOf(DuplicateResourceException.class);

        assertThat(userRepository.count()).isEqualTo(countBefore);
        assertThat(userRepository.findByEmail("rollback_a2@test.local")).isEmpty();
    }

    @Test
    void createReview_duplicatePostAndUser_rollsBackWithoutCreatingSecondReview() {
        User user = userService.register("rollback_user_b", "rollback_b@test.local", "pw", "BLOGGER");
        Post post = postService.create(user.getId(), "Rollback Post", "body", List.of());
        Review first = reviewService.create(post.getId(), user.getId(), 5, "first");
        long countBefore = reviewRepository.count();

        assertThatThrownBy(() ->
                reviewService.create(post.getId(), user.getId(), 1, "second"))
                .isInstanceOf(DuplicateResourceException.class);

        assertThat(reviewRepository.count()).isEqualTo(countBefore);
        Review persisted = reviewRepository.findById(first.getId()).orElseThrow();
        assertThat(persisted.getRating()).isEqualTo(5);
        assertThat(persisted.getContent()).isEqualTo("first");
    }

    @Test
    void createPost_unknownAuthor_rollsBackWithoutPersistingPost() {
        long postCountBefore = postRepository.count();

        assertThatThrownBy(() ->
                postService.create(999_999L, "Ghost", "body", List.of()))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThat(postRepository.count()).isEqualTo(postCountBefore);
    }
}
