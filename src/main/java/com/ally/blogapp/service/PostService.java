package com.ally.blogapp.service;

import com.ally.blogapp.exception.ResourceNotFoundException;
import com.ally.blogapp.model.*;
import com.ally.blogapp.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final TagService tagService;

    public PostService(PostRepository postRepository, UserService userService, TagService tagService) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.tagService = tagService;
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

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Post publish(Long postId) {
        Post post = findById(postId);
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Post archive(Long postId) {
        Post post = findById(postId);
        post.setStatus(PostStatus.ARCHIVED);
        return postRepository.save(post);
    }

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

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public void delete(Long id) {
        findById(id);
        postRepository.deleteById(id);
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Post addTag(Long postId, Long tagId) {
        Post post = findById(postId);
        Tag tag = tagService.findById(tagId);
        post.getTags().add(tag);
        return postRepository.save(post);
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Post removeTag(Long postId, Long tagId) {
        Post post = findById(postId);
        post.getTags().removeIf(t -> t.getId().equals(tagId));
        return postRepository.save(post);
    }
}
