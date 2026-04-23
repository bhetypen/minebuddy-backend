package com.minebuddy.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        @Size(max = 100)
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
        String password,

        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name
) {}