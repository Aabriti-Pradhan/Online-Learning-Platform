package com.finalyearproject.fyp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
        @NotBlank(message = "Username is required")
        @Size(min = 3, message = "Username must be at least 3 characters")
        String username,

        @Email(message = "Enter a valid email")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "Confirm Password is required")
        String confirmPassword,

        @NotBlank(message = "Role must be selected")
        String role
) { }
