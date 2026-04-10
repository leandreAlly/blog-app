package com.ally.blogapp.controller;

import com.ally.blogapp.dto.request.CreatePostRequest;
import com.ally.blogapp.dto.request.UpdatePostRequest;
import com.ally.blogapp.dto.response.ApiResponse;
import com.ally.blogapp.dto.response.PostResponse;
import com.ally.blogapp.model.PostStats;
import com.ally.blogapp.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Posts", description = "Blog post management endpoints")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @Operation(summary = "Get all posts with pagination, sorting, and filtering")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String search) {

        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Page<PostResponse> posts;
        if (search != null && !search.isBlank()) {
            posts = postService.search(search, pageable).map(PostResponse::from);
        } else if (tagId != null) {
            posts = postService.findPublishedByTag(tagId, pageable).map(PostResponse::from);
        } else if ("PUBLISHED".equalsIgnoreCase(status)) {
            posts = postService.findPublished(pageable).map(PostResponse::from);
        } else {
            posts = postService.findAll(pageable).map(PostResponse::from);
        }

        return ResponseEntity.ok(ApiResponse.success("Posts retrieved", posts));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get post by ID")
    public ResponseEntity<ApiResponse<PostResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Post retrieved",
                PostResponse.from(postService.findById(id))));
    }

    @GetMapping("/author/{authorId}")
    @Operation(summary = "Get all posts by author")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getByAuthor(@PathVariable Long authorId) {
        List<PostResponse> posts = postService.findByAuthorId(authorId).stream()
                .map(PostResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Posts retrieved", posts));
    }

    @GetMapping("/author/{authorId}/stats")
    @Operation(summary = "Get post statistics for an author")
    public ResponseEntity<ApiResponse<List<PostStats>>> getStats(@PathVariable Long authorId) {
        return ResponseEntity.ok(ApiResponse.success("Stats retrieved",
                postService.getStatsByAuthorId(authorId)));
    }

    @PostMapping
    @Operation(summary = "Create a new post")
    public ResponseEntity<ApiResponse<PostResponse>> create(@Valid @RequestBody CreatePostRequest req) {
        PostResponse post = PostResponse.from(
                postService.create(req.authorId(), req.title(), req.content(), req.tags()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Post created", post));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a post")
    public ResponseEntity<ApiResponse<PostResponse>> update(@PathVariable Long id,
                                                            @Valid @RequestBody UpdatePostRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Post updated",
                PostResponse.from(postService.update(id, req.title(), req.content(), req.tags()))));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a post")
    public ResponseEntity<ApiResponse<PostResponse>> publish(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Post published",
                PostResponse.from(postService.publish(id))));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive a post")
    public ResponseEntity<ApiResponse<PostResponse>> archive(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Post archived",
                PostResponse.from(postService.archive(id))));
    }

    @PostMapping("/{postId}/tags/{tagId}")
    @Operation(summary = "Add a tag to a post")
    public ResponseEntity<ApiResponse<PostResponse>> addTag(@PathVariable Long postId,
                                                            @PathVariable Long tagId) {
        return ResponseEntity.ok(ApiResponse.success("Tag added",
                PostResponse.from(postService.addTag(postId, tagId))));
    }

    @DeleteMapping("/{postId}/tags/{tagId}")
    @Operation(summary = "Remove a tag from a post")
    public ResponseEntity<ApiResponse<PostResponse>> removeTag(@PathVariable Long postId,
                                                               @PathVariable Long tagId) {
        return ResponseEntity.ok(ApiResponse.success("Tag removed",
                PostResponse.from(postService.removeTag(postId, tagId))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a post")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Post deleted", null));
    }
}
