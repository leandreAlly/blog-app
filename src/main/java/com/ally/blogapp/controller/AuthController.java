package com.ally.blogapp.controller;

import com.ally.blogapp.dto.request.CreateUserRequest;
import com.ally.blogapp.dto.request.LoginRequest;
import com.ally.blogapp.dto.response.ApiResponse;
import com.ally.blogapp.dto.response.UserResponse;
import com.ally.blogapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and registration endpoints")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest req) {
        UserResponse user = UserResponse.from(userService.login(req.username(), req.password()));
        return ResponseEntity.ok(ApiResponse.success("Login successful", user));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody CreateUserRequest req) {
        UserResponse user = UserResponse.from(
                userService.register(req.username(), req.email(), req.password(), req.role()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Registration successful", user));
    }
}
