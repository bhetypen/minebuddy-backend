package com.minebuddy.dto.response;

import com.minebuddy.model.enums.SaleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemResponseDTO(
        String itemId,
        String name,
        String liveName,
        String category,
        SaleType saleType,
        BigDecimal price,
        BigDecimal cost,
        int stock,
        boolean active,
        LocalDateTime createdAt
) {}
