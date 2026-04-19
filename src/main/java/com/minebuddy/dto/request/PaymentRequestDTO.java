package com.minebuddy.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequestDTO(
        @NotNull(message = "Order ID is required")
        UUID orderId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        @Digits(integer = 8, fraction = 2, message = "Amount must have at most 2 decimal places")
        BigDecimal amount,

        @NotBlank(message = "Payment method is required")
        @Pattern(regexp = "CASH|GCASH|BANK|CARD|MAYA",
                message = "Method must be one of: CASH, GCASH, BANK, CARD, MAYA")
        String method,

        @Size(max = 100, message = "Reference must be 100 characters or less")
        String reference,

        @Size(max = 500, message = "Receipt URL must be 500 characters or less")
        String receiptUrl
) {}
