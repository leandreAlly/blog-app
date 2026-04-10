package com.ally.blogapp.controller;

import com.ally.blogapp.dto.request.CreateCommentRequest;
import com.ally.blogapp.dto.request.UpdateCommentRequest;
import com.ally.blogapp.dto.response.ApiResponse;
import com.ally.blogapp.dto.response.CommentResponse;
import com.ally.blogapp.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@Tag(name = "Comments", description = "Comment management endpoints")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @Operation(summary = "Get all comments for a post")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getByPost(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.findByPostId(postId).stream()
                .map(CommentResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved", comments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get comment by ID")
    public ResponseEntity<ApiResponse<CommentResponse>> getById(@PathVariable Long postId,
                                                                 @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Comment retrieved",
                CommentResponse.from(commentService.findById(id))));
    }

    @PostMapping
    @Operation(summary = "Add a comment to a post")
    public ResponseEntity<ApiResponse<CommentResponse>> create(@PathVariable Long postId,
                                                               @Valid @RequestBody CreateCommentRequest req) {
        CommentResponse comment = CommentResponse.from(
                commentService.create(postId, req.userId(), req.content()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Comment created", comment));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a comment")
    public ResponseEntity<ApiResponse<CommentResponse>> update(@PathVariable Long postId,
                                                               @PathVariable Long id,
                                                               @Valid @RequestBody UpdateCommentRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Comment updated",
                CommentResponse.from(commentService.update(id, req.content()))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long postId,
                                                    @PathVariable Long id) {
        commentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted", null));
    }
}
