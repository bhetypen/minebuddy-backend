package com.minebuddy.dto.response;

import java.time.LocalDateTime;

public record UserResponseDTO(
        String userId,
        String email,
        String name,
        boolean active,
        boolean superAdmin,
        LocalDateTime createdAt
) {}