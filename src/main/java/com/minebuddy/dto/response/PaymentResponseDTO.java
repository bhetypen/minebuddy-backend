package com.minebuddy.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDTO(
        String paymentId,
        String orderId,
        BigDecimal amount,
        String paymentMethod,
        String paymentReference,
        String receiptUrl,
        LocalDateTime paymentDate,
        LocalDateTime paymentUpdatedDate
) {}