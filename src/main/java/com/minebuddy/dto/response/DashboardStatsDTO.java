package com.minebuddy.dto.response;

import java.math.BigDecimal;

public record DashboardStatsDTO(
        // Money
        BigDecimal grossRevenueThisMonth,
        BigDecimal netProfitThisMonth,
        BigDecimal uncollectedBalance,

        // Work queue
        long ordersToShip,
        long ordersInTransit,

        // Inventory & customers
        long lowStockItems,
        long activeCustomers
) {}