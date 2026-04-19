package com.minebuddy.dto.request;

import com.minebuddy.model.enums.SaleType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ItemRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be 255 characters or less")
        String name,

        @Size(max = 50, message = "Live name must be 50 characters or less")
        String liveName,

        @NotBlank(message = "Category is required")
        @Size(max = 100, message = "Category must be 100 characters or less")
        String category,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", message = "Price must be non-negative")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 2 decimal places")
        BigDecimal price,

        @Min(value = 0, message = "Stock must be non-negative")
        int stock,

        @NotNull(message = "Sale type is required")
        SaleType saleType
) {}