package com.minebuddy.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDTO(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}