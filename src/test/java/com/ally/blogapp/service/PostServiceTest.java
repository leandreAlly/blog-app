package com.ally.blogapp.service;

import com.ally.blogapp.exception.ResourceNotFoundException;
import com.ally.blogapp.model.*;
import com.ally.blogapp.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private UserService userService;
    @Mock private TagService tagService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private TrendingIndex trendingIndex;

    @InjectMocks
    private PostService postService;

    @Test
    void create_shouldSavePost_withDraftStatus() {
        User author = new User(); author.setId(1L);
        when(userService.findById(1L)).thenReturn(author);
        Post saved = new Post(author, "Hello", "World");
        saved.setId(5L);
        when(postRepository.save(any())).thenReturn(saved);

        Post result = postService.create(1L, "Hello", "World", null);

        assertThat(result.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(result.getTitle()).isEqualTo("Hello");
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void publish_shouldSetStatusAndTimestamp() {
        Post post = new Post(); post.setId(1L); post.setStatus(PostStatus.DRAFT);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Post result = postService.publish(1L);

        assertThat(result.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(result.getPublishedAt()).isNotNull();
    }

    @Test
    void archive_shouldSetArchivedStatus() {
        Post post = new Post(); post.setId(1L); post.setStatus(PostStatus.PUBLISHED);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Post result = postService.archive(1L);

        assertThat(result.getStatus()).isEqualTo(PostStatus.ARCHIVED);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByAuthorId_shouldReturnPosts() {
        when(postRepository.findByAuthorIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(new Post(), new Post()));

        assertThat(postService.findByAuthorId(1L)).hasSize(2);
    }
}
