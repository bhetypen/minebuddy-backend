package com.minebuddy.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipmentResponseDTO(
        String shipmentId,
        String orderId,
        String carrier,
        String trackingNumber,
        BigDecimal shippingFee,
        String shipmentStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
