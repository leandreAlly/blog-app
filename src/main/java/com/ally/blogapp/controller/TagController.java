package com.ally.blogapp.controller;

import com.ally.blogapp.dto.request.CreateTagRequest;
import com.ally.blogapp.dto.response.ApiResponse;
import com.ally.blogapp.dto.response.TagResponse;
import com.ally.blogapp.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "Tags", description = "Tag management endpoints")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    @Operation(summary = "Get all tags with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<TagResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        Page<TagResponse> tags = tagService.findAll(pageable).map(TagResponse::from);
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved", tags));
    }

    @GetMapping("/search")
    @Operation(summary = "Search tags by name fragment (case-insensitive)")
    public ResponseEntity<ApiResponse<Page<TagResponse>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<TagResponse> tags = tagService.search(q, pageable).map(TagResponse::from);
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved", tags));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get most-used tags ordered by post count")
    public ResponseEntity<ApiResponse<List<TagResponse>>> popular(
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<TagResponse> tags = tagService.findPopular(pageable).stream()
                .map(TagResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Popular tags retrieved", tags));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID")
    public ResponseEntity<ApiResponse<TagResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Tag retrieved",
                TagResponse.from(tagService.findById(id))));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new tag")
    public ResponseEntity<ApiResponse<TagResponse>> create(@Valid @RequestBody CreateTagRequest req) {
        TagResponse tag = TagResponse.from(tagService.create(req.name()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tag created", tag));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a tag")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Tag deleted", null));
    }
}
