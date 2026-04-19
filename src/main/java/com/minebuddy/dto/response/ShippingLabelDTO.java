package com.minebuddy.dto.response;

import java.math.BigDecimal;

public record ShippingLabelDTO(
        String orderId,
        String itemId,
        String customerName,
        String customerPhone,
        String fullAddress,
        String landmark,
        String itemName,
        int quantity,
        BigDecimal balanceDue,
        String orderStatus
) {}
