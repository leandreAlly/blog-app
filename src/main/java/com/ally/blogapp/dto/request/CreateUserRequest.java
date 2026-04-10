package com.ally.blogapp.dto.request;

import com.ally.blogapp.validation.UniqueEmail;
import com.ally.blogapp.validation.UniqueUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @UniqueUsername
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @UniqueEmail
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "BLOGGER|READER", message = "Role must be BLOGGER or READER")
        String role
) {}
