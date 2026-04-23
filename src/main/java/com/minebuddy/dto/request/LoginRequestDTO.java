package com.minebuddy.dto.request;

import jakarta.validation.constraints.*;

public record LoginRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}