package com.minebuddy.dto.response;

import java.util.UUID;

public record CustomerResponseDTO(
        UUID customerId,
        String fullName, String handle,
        String platform, String phone,
        UUID addressId) {
}
