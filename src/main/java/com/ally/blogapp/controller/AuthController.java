package com.ally.blogapp.controller;

import com.ally.blogapp.dto.request.CreateUserRequest;
import com.ally.blogapp.dto.request.LoginRequest;
import com.ally.blogapp.dto.response.ApiResponse;
import com.ally.blogapp.dto.response.AuthResponse;
import com.ally.blogapp.dto.response.UserResponse;
import com.ally.blogapp.security.JwtUtils;
import com.ally.blogapp.security.TokenBlacklistService;
import com.ally.blogapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and registration endpoints")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username and password, returns a signed JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String token = jwtUtils.generateToken(auth.getName(), role);

        AuthResponse body = new AuthResponse(
                token,
                auth.getName(),
                role,
                jwtUtils.getExpirationMs() / 1000
        );
        return ResponseEntity.ok(ApiResponse.success("Login successful", body));
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate the current JWT token")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                tokenBlacklistService.blacklist(token, jwtUtils.extractExpiration(token));
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody CreateUserRequest req
        ){


        UserResponse user = UserResponse.from(
                userService.register(req.username(), req.email(), req.password(), req.role()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Registration successful", user));
    }
}
