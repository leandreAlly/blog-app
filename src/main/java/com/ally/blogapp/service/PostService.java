package com.ally.blogapp.service;

import com.ally.blogapp.config.AsyncConfig;
import com.ally.blogapp.config.CacheConfig;
import com.ally.blogapp.event.PostPublishedEvent;
import com.ally.blogapp.exception.ResourceNotFoundException;
import com.ally.blogapp.model.*;
import com.ally.blogapp.repository.PostRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final TagService tagService;
    private final ApplicationEventPublisher eventPublisher;

    public PostService(PostRepository postRepository,
                       UserService userService,
                       TagService tagService,
                       ApplicationEventPublisher eventPublisher) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.tagService = tagService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Post create(Long authorId, String title, String content, List<String> tagNames) {
        User author = userService.findById(authorId);
        Post post = new Post(author, title, content);
        if (tagNames != null) {
            Set<Tag> tags = tagNames.stream()
                    .map(tagService::findOrCreate)
                    .collect(Collectors.toSet());
            post.setTags(tags);
        }
        return postRepository.save(post);
    }

    @Cacheable(value = CacheConfig.POSTS, key = "#id")
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    public List<Post> findByAuthorId(Long authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    public Page<Post> findPublished(Pageable pageable) {
        return postRepository.findByStatus(PostStatus.PUBLISHED, pageable);
    }

    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Page<Post> search(String keyword, Pageable pageable) {
        return postRepository.searchByKeyword(keyword, pageable);
    }

    public Page<Post> findPublishedByTag(Long tagId, Pageable pageable) {
        return postRepository.findPublishedByTagId(tagId, pageable);
    }

    public List<PostStats> getStatsByAuthorId(Long authorId) {
        return postRepository.getStatsByAuthorId(authorId);
    }

    public List<Post> findTrending(int limit) {
        return postRepository.findTrending(limit);
    }

    /**
     * Async variant for the REST endpoint. The aggregation joins posts
     * with comments and reviews and groups by post — expensive enough that
     * blocking a Tomcat worker on it limits concurrency. Offloading to
     * analyticsExecutor lets the request thread return immediately and the
     * pool absorbs concurrent stat requests up to its bounded queue.
     */
    @Async(AsyncConfig.ANALYTICS_EXECUTOR)
    public CompletableFuture<List<PostStats>> getStatsByAuthorIdAsync(Long authorId) {
        return CompletableFuture.completedFuture(postRepository.getStatsByAuthorId(authorId));
    }

    @Async(AsyncConfig.ANALYTICS_EXECUTOR)
    public CompletableFuture<List<Post>> findTrendingAsync(int limit) {
        return CompletableFuture.completedFuture(postRepository.findTrending(limit));
    }

    @CacheEvict(value = CacheConfig.POSTS, key = "#postId")
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Post publish(Long postId) {
        Post post = findById(postId);
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        Post saved = postRepository.save(post);
        eventPublisher.publishEvent(
                new PostPublishedEvent(saved.getId(), saved.getAuthor().getId(), saved.getTitle()));
        return saved;
    }

    @CacheEvict(value = CacheConfig.POSTS, key = "#postId")
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Post archive(Long postId) {
        Post post = findById(postId);
        post.setStatus(PostStatus.ARCHIVED);
        return postRepository.save(post);
    }

    @CacheEvict(value = CacheConfig.POSTS, key = "#id")
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Post update(Long id, String title, String content, List<String> tagNames) {
        Post post = findById(id);
        if (title != null) post.setTitle(title);
        if (content != null) post.setContent(content);
        if (tagNames != null) {
            Set<Tag> tags = tagNames.stream()
                    .map(tagService::findOrCreate)
                    .collect(Collectors.toSet());
            post.setTags(tags);
        }
        return postRepository.save(post);
    }

    @CacheEvict(value = CacheConfig.POSTS, key = "#id")
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public void delete(Long id) {
        findById(id);
        postRepository.deleteById(id);
    }

    @CacheEvict(value = CacheConfig.POSTS, key = "#postId")
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Post addTag(Long postId, Long tagId) {
        Post post = findById(postId);
        Tag tag = tagService.findById(tagId);
        post.getTags().add(tag);
        return postRepository.save(post);
    }

    @CacheEvict(value = CacheConfig.POSTS, key = "#postId")
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Post removeTag(Long postId, Long tagId) {
        Post post = findById(postId);
        post.getTags().removeIf(t -> t.getId().equals(tagId));
        return postRepository.save(post);
    }
}
