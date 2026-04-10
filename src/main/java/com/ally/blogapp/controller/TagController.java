package com.ally.blogapp.controller;

import com.ally.blogapp.dto.request.CreateTagRequest;
import com.ally.blogapp.dto.response.ApiResponse;
import com.ally.blogapp.dto.response.TagResponse;
import com.ally.blogapp.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @Operation(summary = "Get all tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAll() {
        List<TagResponse> tags = tagService.findAll().stream()
                .map(TagResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved", tags));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID")
    public ResponseEntity<ApiResponse<TagResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Tag retrieved",
                TagResponse.from(tagService.findById(id))));
    }

    @PostMapping
    @Operation(summary = "Create a new tag")
    public ResponseEntity<ApiResponse<TagResponse>> create(@Valid @RequestBody CreateTagRequest req) {
        TagResponse tag = TagResponse.from(tagService.create(req.name()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tag created", tag));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tag")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Tag deleted", null));
    }
}
