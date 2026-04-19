package com.minebuddy.dto.response;

import com.minebuddy.model.enums.OrderStatus;
import com.minebuddy.model.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponseDTO(
        String orderId,
        String customerId,
        String itemId,
        int quantity,
        PaymentType paymentType,
        BigDecimal unitPriceAtOrderTime,
        BigDecimal totalAmount,
        BigDecimal dpRequired,
        BigDecimal dpPaid,
        BigDecimal finalPaid,
        BigDecimal balance,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
