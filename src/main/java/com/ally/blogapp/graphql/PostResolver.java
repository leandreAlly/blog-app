package com.ally.blogapp.graphql;

import com.ally.blogapp.model.Post;
import com.ally.blogapp.model.PostStats;
import com.ally.blogapp.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class PostResolver {

    private final PostService postService;

    public PostResolver(PostService postService) {
        this.postService = postService;
    }

    @QueryMapping
    public Post post(@Argument Long id) {
        return postService.findById(id);
    }

    @QueryMapping
    public Map<String, Object> posts(@Argument Integer page, @Argument Integer size,
                                     @Argument String sort, @Argument String direction) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 10;
        String sortField = sort != null ? sort : "createdAt";
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(p, s, Sort.by(dir, sortField));
        return toPageMap(postService.findAll(pageable));
    }

    @QueryMapping
    public Map<String, Object> publishedPosts(@Argument Integer page, @Argument Integer size) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return toPageMap(postService.findPublished(pageable));
    }

    @QueryMapping
    public Map<String, Object> postsByTag(@Argument Long tagId, @Argument Integer page,
                                          @Argument Integer size) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 10);
        return toPageMap(postService.findPublishedByTag(tagId, pageable));
    }

    @QueryMapping
    public Map<String, Object> searchPosts(@Argument String keyword, @Argument Integer page,
                                           @Argument Integer size) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 10);
        return toPageMap(postService.search(keyword, pageable));
    }

    @QueryMapping
    public List<Post> postsByAuthor(@Argument Long authorId) {
        return postService.findByAuthorId(authorId);
    }

    @QueryMapping
    public List<PostStats> postStats(@Argument Long authorId) {
        return postService.getStatsByAuthorId(authorId);
    }

    @MutationMapping
    public Post createPost(@Argument Long authorId, @Argument String title,
                           @Argument String content, @Argument List<String> tags) {
        return postService.create(authorId, title, content, tags);
    }

    @MutationMapping
    public Post updatePost(@Argument Long id, @Argument String title,
                           @Argument String content, @Argument List<String> tags) {
        return postService.update(id, title, content, tags);
    }

    @MutationMapping
    public Post publishPost(@Argument Long id) {
        return postService.publish(id);
    }

    @MutationMapping
    public Post archivePost(@Argument Long id) {
        return postService.archive(id);
    }

    @MutationMapping
    public Post addTagToPost(@Argument Long postId, @Argument Long tagId) {
        return postService.addTag(postId, tagId);
    }

    @MutationMapping
    public Post removeTagFromPost(@Argument Long postId, @Argument Long tagId) {
        return postService.removeTag(postId, tagId);
    }

    @MutationMapping
    public Boolean deletePost(@Argument Long id) {
        postService.delete(id);
        return true;
    }

    private Map<String, Object> toPageMap(Page<Post> page) {
        return Map.of(
            "content", page.getContent(),
            "pageInfo", Map.of(
                "totalElements", (int) page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "currentPage", page.getNumber(),
                "pageSize", page.getSize()
            )
        );
    }
}
