package com.minebuddy.dto;

import com.minebuddy.model.enums.OrderStatus;
import com.minebuddy.model.enums.PaymentType;

import java.math.BigDecimal;

public record OrderSummaryDTO(
        String orderId,
        String customerName,
        String itemName,
        int quantity,
        BigDecimal totalAmount,
        BigDecimal dpPaid,
        BigDecimal finalPaid,
        BigDecimal balance,
        OrderStatus status,
        PaymentType paymentType,
        String trackingNumber,
        String shipmentStatus
) {}
