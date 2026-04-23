package com.minebuddy.service;

import com.minebuddy.dto.response.DashboardStatsDTO;
import com.minebuddy.model.Order;
import com.minebuddy.model.Shipment;
import com.minebuddy.model.enums.OrderStatus;
import com.minebuddy.repository.CustomerRepository;
import com.minebuddy.repository.ItemRepository;
import com.minebuddy.repository.OrderRepository;
import com.minebuddy.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private static final Set<OrderStatus> TERMINAL_STATUSES =
            Set.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED);

    private static final Set<OrderStatus> SHIP_READY_STATUSES =
            Set.of(OrderStatus.FULLY_PAID, OrderStatus.PACKED);

    private static final Set<String> IN_TRANSIT_SHIPMENT_STATUSES =
            Set.of("PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY");

    private final OrderRepository orderRepo;
    private final ShipmentRepository shipmentRepo;
    private final ItemRepository itemRepo;
    private final CustomerRepository customerRepo;

    public DashboardService(OrderRepository orderRepo,
                            ShipmentRepository shipmentRepo,
                            ItemRepository itemRepo,
                            CustomerRepository customerRepo) {
        this.orderRepo = orderRepo;
        this.shipmentRepo = shipmentRepo;
        this.itemRepo = itemRepo;
        this.customerRepo = customerRepo;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        List<Order> allOrders = orderRepo.findAll();
        List<Shipment> allShipments = shipmentRepo.findAll();

        return new DashboardStatsDTO(
                computeGrossRevenueThisMonth(allOrders),
                computeUncollectedBalance(allOrders),
                computeOrdersToShip(allOrders, allShipments),
                computeOrdersInTransit(allShipments),
                computeLowStockItems(),
                customerRepo.count()
        );
    }

    // Sum of totalAmount for COMPLETED orders whose createdAt is in the current month.
    private BigDecimal computeGrossRevenueThisMonth(List<Order> orders) {
        LocalDateTime monthStart = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();

        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(monthStart))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Sum of balance on orders that are not terminal (still collectible).
    private BigDecimal computeUncollectedBalance(List<Order> orders) {
        return orders.stream()
                .filter(o -> !TERMINAL_STATUSES.contains(o.getStatus()))
                .map(Order::getBalance)
                .filter(b -> b.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Orders that are FULLY_PAID or PACKED but don't yet have a shipment record.
    private long computeOrdersToShip(List<Order> orders, List<Shipment> shipments) {
        Set<UUID> orderIdsWithShipments = shipments.stream()
                .map(Shipment::getOrderId)
                .collect(Collectors.toSet());

        return orders.stream()
                .filter(o -> SHIP_READY_STATUSES.contains(o.getStatus()))
                .filter(o -> !orderIdsWithShipments.contains(o.getOrderId()))
                .count();
    }

    // Shipments that are PICKED_UP, IN_TRANSIT, or OUT_FOR_DELIVERY.
    private long computeOrdersInTransit(List<Shipment> shipments) {
        return shipments.stream()
                .filter(s -> IN_TRANSIT_SHIPMENT_STATUSES.contains(s.getShipmentStatus()))
                .count();
    }

    // Items where stock is below the low-stock threshold AND the item is still active.
    private long computeLowStockItems() {
        return itemRepo.findAll().stream()
                .filter(i -> i.isActive())
                .filter(i -> i.getStock() < LOW_STOCK_THRESHOLD)
                .count();
    }
}