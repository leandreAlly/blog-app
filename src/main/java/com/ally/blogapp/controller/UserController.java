package com.ally.blogapp.controller;

import com.ally.blogapp.dto.request.CreateUserRequest;
import com.ally.blogapp.dto.request.UpdateUserRequest;
import com.ally.blogapp.dto.response.ApiResponse;
import com.ally.blogapp.dto.response.UserResponse;
import com.ally.blogapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        List<UserResponse> users = userService.findAll().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User retrieved",
                UserResponse.from(userService.findById(id))));
    }

    @PostMapping
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest req) {
        UserResponse user = UserResponse.from(
                userService.register(req.username(), req.email(), req.password(), req.role()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("User created", user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(ApiResponse.success("User updated",
                UserResponse.from(userService.update(id, req.bio(), req.profileImage()))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }
}
