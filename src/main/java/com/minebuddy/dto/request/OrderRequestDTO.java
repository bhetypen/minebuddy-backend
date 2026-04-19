package com.minebuddy.dto.request;

import com.minebuddy.model.enums.PaymentType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderRequestDTO(
        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotNull(message = "Item ID is required")
        UUID itemId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        @NotNull(message = "Payment type is required")
        PaymentType paymentType,

        @DecimalMin(value = "0.00", message = "Down payment required must be non-negative")
        @Digits(integer = 8, fraction = 2)
        BigDecimal dpRequired
) {}
