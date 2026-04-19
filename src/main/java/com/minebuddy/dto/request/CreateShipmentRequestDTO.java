package com.minebuddy.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateShipmentRequestDTO(
        @NotNull(message = "Order ID is required")
        UUID orderId,

        @NotBlank(message = "Carrier is required")
        @Size(max = 100, message = "Carrier must be 100 characters or less")
        String carrier,

        @Size(max = 100, message = "Tracking number must be 100 characters or less")
        String trackingNumber,

        @NotNull(message = "Shipping fee is required")
        @DecimalMin(value = "0.00", message = "Shipping fee must be non-negative")
        @Digits(integer = 8, fraction = 2)
        BigDecimal shippingFee
) {}
