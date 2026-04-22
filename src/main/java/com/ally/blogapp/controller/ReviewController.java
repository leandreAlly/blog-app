package com.ally.blogapp.controller;

import com.ally.blogapp.dto.request.CreateReviewRequest;
import com.ally.blogapp.dto.request.UpdateReviewRequest;
import com.ally.blogapp.dto.response.ApiResponse;
import com.ally.blogapp.dto.response.ReviewResponse;
import com.ally.blogapp.service.ReviewService;
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

@RestController
@RequestMapping("/api/posts/{postId}/reviews")
@Tag(name = "Reviews", description = "Review management endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    @Operation(summary = "Get reviews for a post with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        Page<ReviewResponse> reviews = reviewService.findByPostId(postId, pageable).map(ReviewResponse::from);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", reviews));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<ApiResponse<ReviewResponse>> getById(@PathVariable Long postId,
                                                               @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Review retrieved",
                ReviewResponse.from(reviewService.findById(id))));
    }

    @GetMapping("/average")
    @Operation(summary = "Get average rating for a post")
    public ResponseEntity<ApiResponse<Double>> getAverage(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("Average rating retrieved",
                reviewService.getAverageRating(postId)));
    }

    @PostMapping
    @Operation(summary = "Add a review to a post")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@PathVariable Long postId,
                                                              @Valid @RequestBody CreateReviewRequest req) {
        ReviewResponse review = ReviewResponse.from(
                reviewService.create(postId, req.userId(), req.rating(), req.content()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Review created", review));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a review")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(@PathVariable Long postId,
                                                              @PathVariable Long id,
                                                              @Valid @RequestBody UpdateReviewRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Review updated",
                ReviewResponse.from(reviewService.update(id, req.rating(), req.content()))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long postId,
                                                    @PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }
}
