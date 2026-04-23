package com.minebuddy.dto.response;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String userId,
        String email,
        String name,
        boolean superAdmin
) {}