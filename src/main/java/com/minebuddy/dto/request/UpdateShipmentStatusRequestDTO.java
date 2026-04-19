package com.minebuddy.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateShipmentStatusRequestDTO(
        @NotBlank(message = "New status is required")
        String newStatus,

        String trackingNumber
) {}
