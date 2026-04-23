package com.minebuddy.service;

import com.minebuddy.dto.request.OrderRequestDTO;
import com.minebuddy.dto.response.OrderResponseDTO;
import com.minebuddy.dto.OrderSummaryDTO;
import com.minebuddy.model.Item;
import com.minebuddy.model.Order;
import com.minebuddy.model.Shipment;
import com.minebuddy.model.enums.OrderStatus;
import com.minebuddy.model.enums.PaymentType;
import com.minebuddy.model.enums.SaleType;
import com.minebuddy.repository.CustomerRepository;
import com.minebuddy.repository.ItemRepository;
import com.minebuddy.repository.OrderRepository;
import com.minebuddy.repository.ShipmentRepository;
import com.minebuddy.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private static final BigDecimal DP_RATE = new BigDecimal("0.30");

    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final ItemRepository itemRepo;
    private final ShipmentRepository shipmentRepo;

    private String message;

    public OrderService(OrderRepository orderRepo,
                        CustomerRepository customerRepo,
                        ItemRepository itemRepo,
                        ShipmentRepository shipmentRepo) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.itemRepo = itemRepo;
        this.shipmentRepo = shipmentRepo;
    }

    public String getMessage() {
        return message;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        UUID storeId = TenantContext.getStoreId();
        if (!customerRepo.existsByCustomerIdAndStoreId(request.customerId(), storeId)) {
            this.message = "Customer not found.";
            return null;
        }

        Item item = itemRepo.findByItemIdAndStoreId(request.itemId(), storeId).orElse(null);
        if (item == null || !item.isActive()) {
            this.message = "Item not found or inactive.";
            return null;
        }

        return switch (item.getSaleType()) {
            case ONHAND_ONLY -> createOnhandOrder(request, item);
            case PREORDER_ONLY -> createPreorderOrder(request, item);
            case HYBRID -> createHybridOrder(request, item);
        };
    }

    @Transactional
    public boolean updateStatus(UUID orderId, OrderStatus nextStatus) {
        UUID storeId = TenantContext.getStoreId();
        Order order = orderRepo.findByOrderIdAndStoreId(orderId, storeId).orElse(null);
        if (order == null || nextStatus == null) {
            this.message = "Order not found.";
            return false;
        }

        if (isTerminal(order.getStatus())) {
            this.message = "Order is already finalized: " + order.getStatus();
            return false;
        }

        if (nextStatus != OrderStatus.FULLY_PAID && nextStatus != OrderStatus.CANCELLED) {
            boolean isArrivingAfterPayment =
                    order.getStatus() == OrderStatus.FULLY_PAID && nextStatus == OrderStatus.ARRIVED;

            if (!isArrivingAfterPayment && rank(nextStatus) < rank(order.getStatus())) {
                this.message = "Sequence Error: Cannot move backward from " + order.getStatus();
                return false;
            }
        }

        Item item = itemRepo.findByItemIdAndStoreId(order.getItemId(), storeId).orElse(null);
        if (item == null) {
            this.message = "Item data missing; cannot validate workflow.";
            return false;
        }

        if (nextStatus == OrderStatus.DP_PAID
                || nextStatus == OrderStatus.FOR_ORDERING
                || nextStatus == OrderStatus.ORDERED_FROM_SUPPLIER) {
            if (order.getPaymentType() == PaymentType.PREORDER) {
                BigDecimal totalPaidSoFar = order.getTotalPaid();
                if (totalPaidSoFar.compareTo(order.getDpRequired()) < 0) {
                    this.message = "Financial Error: Total paid (P" + totalPaidSoFar
                            + ") does not meet DP requirement (P" + order.getDpRequired() + ").";
                    return false;
                }
            }
        }

        if (nextStatus == OrderStatus.SHIPPED) {
            if (order.getStatus() != OrderStatus.PACKED) {
                this.message = "Workflow Error: Must pack the order before shipping.";
                return false;
            }

            boolean hasTracking = shipmentRepo.findByOrderIdAndStoreId(orderId, storeId)
                    .map(s -> s.getTrackingNumber() != null && !s.getTrackingNumber().isBlank())
                    .orElse(false);

            if (!hasTracking) {
                this.message = "Logistics Error: Cannot set to SHIPPED without a Tracking Number.";
                return false;
            }
        }

        if (rank(nextStatus) >= rank(OrderStatus.PACKED) && nextStatus != OrderStatus.CANCELLED) {
            boolean needsArrival = item.getSaleType() == SaleType.PREORDER_ONLY
                    || (item.getSaleType() == SaleType.HYBRID
                    && rank(order.getStatus()) >= rank(OrderStatus.ORDERED_FROM_SUPPLIER));

            if (needsArrival && order.getStatus() != OrderStatus.ARRIVED) {
                this.message = "Logistics Error: Item has not arrived from the supplier yet.";
                return false;
            }
        }

        if (rank(nextStatus) >= rank(OrderStatus.SHIPPED) && nextStatus != OrderStatus.CANCELLED) {
            if (order.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                this.message = "Financial Error: Balance must be zero before shipping.";
                return false;
            }
        }

        if (rank(nextStatus) >= rank(OrderStatus.COMPLETED)) {
            if (order.getStatus() != OrderStatus.SHIPPED) {
                this.message = "Fulfillment Error: Cannot complete order without shipping.";
                return false;
            }

            Optional<Shipment> shipmentOpt = shipmentRepo.findByOrderIdAndStoreId(orderId, storeId);
            if (shipmentOpt.isEmpty()) {
                this.message = "Fulfillment Error: Cannot complete order without shipment.";
                return false;
            }

            Shipment shipment = shipmentOpt.get();
            if (!"DELIVERED".equals(shipment.getShipmentStatus())) {
                this.message = String.format(
                        "Logistics Error: Cannot complete order. Shipment is currently '%s'. "
                                + "Update shipment to DELIVERED first.",
                        shipment.getShipmentStatus()
                );
                return false;
            }
        }

        order.setStatus(nextStatus);
        orderRepo.save(order);

        this.message = "Order " + order.getOrderId() + " updated to " + nextStatus;
        return true;
    }

    @Transactional
    public boolean editOrder(UUID orderId, UUID newItemId, int newQty, BigDecimal newShippingFee) {
        UUID storeId = TenantContext.getStoreId();
        Order order = orderRepo.findByOrderIdAndStoreId(orderId, storeId).orElse(null);

        if (order == null || rank(order.getStatus()) >= 4 || newQty <= 0) {
            this.message = "Edit denied: Order is "
                    + (order != null ? order.getStatus() : "not found") + ".";
            return false;
        }

        Item newItem = itemRepo.findByItemIdAndStoreId(newItemId, storeId).orElse(null);
        if (newItem == null) {
            this.message = "New item not found.";
            return false;
        }

        if (newItem.getSaleType() == SaleType.ONHAND_ONLY && newItem.getStock() < newQty) {
            this.message = "Insufficient stock for strict on-hand item.";
            return false;
        }

        itemRepo.findByItemIdAndStoreId(order.getItemId(), storeId).ifPresent(oldItem -> {
            if (oldItem.getSaleType() != SaleType.PREORDER_ONLY) {
                oldItem.increaseStock(order.getQuantity());
                itemRepo.save(oldItem);
            }
        });

        if (newItem.getSaleType() != SaleType.PREORDER_ONLY && newItem.getStock() >= newQty) {
            newItem.decreaseStock(newQty);
            itemRepo.save(newItem);
        }

        BigDecimal newItemTotal = newItem.getPrice().multiply(BigDecimal.valueOf(newQty));
        BigDecimal finalShippingFee = (newShippingFee != null) ? newShippingFee : order.getShippingFee();
        BigDecimal newTotal = newItemTotal.add(finalShippingFee);

        BigDecimal newDpReq = order.getPaymentType() == PaymentType.PREORDER
                ? newTotal.multiply(DP_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        order.setItemId(newItemId);
        order.setQuantity(newQty);
        order.setUnitPriceAtOrderTime(newItem.getPrice());
        order.setItemTotal(newItemTotal);
        order.setShippingFee(finalShippingFee);
        order.setDpRequired(newDpReq);
        // totalAmount and balance are recalculated automatically via setter cascades

        if (order.getStatus() == OrderStatus.DP_PAID && newDpReq.compareTo(order.getDpPaid()) > 0) {
            order.setStatus(OrderStatus.RESERVED);
        }
        if (order.getStatus() == OrderStatus.FULLY_PAID && newTotal.compareTo(order.getTotalPaid()) > 0) {
            order.setStatus(OrderStatus.ARRIVED);
        }

        orderRepo.save(order);
        this.message = "Order " + orderId + " updated.";
        return true;
    }

    @Transactional
    public boolean cancelOrder(UUID orderId) {
        UUID storeId = TenantContext.getStoreId();
        Order order = orderRepo.findByOrderIdAndStoreId(orderId, storeId).orElse(null);
        if (order == null || isTerminal(order.getStatus())) {
            this.message = "Order cannot be cancelled.";
            return false;
        }

        if (order.getPaymentType() == PaymentType.PREORDER
                && order.getDpPaid().compareTo(BigDecimal.ZERO) > 0) {
            this.message = "Cannot cancel: Deposit already paid.";
            return false;
        }

        itemRepo.findByItemIdAndStoreId(order.getItemId(), storeId).ifPresent(item -> {
            if (item.getSaleType() != SaleType.PREORDER_ONLY) {
                item.increaseStock(order.getQuantity());
                itemRepo.save(item);
            }
        });

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
        this.message = "Order cancelled.";
        return true;
    }

    @Transactional
    public int processBatchArrival(UUID itemId) {
        UUID storeId = TenantContext.getStoreId();
        List<Order> orders = orderRepo.findByItemIdAndStatusAndStoreId(itemId, OrderStatus.ORDERED_FROM_SUPPLIER, storeId);

        for (Order order : orders) {
            order.setStatus(OrderStatus.ARRIVED);
            orderRepo.save(order);
        }

        return orders.size();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> findAll() {
        UUID storeId = TenantContext.getStoreId();
        return orderRepo.findAllByStoreId(storeId).stream().map(this::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public Optional<OrderResponseDTO> findById(UUID id) {
        UUID storeId = TenantContext.getStoreId();
        return orderRepo.findByOrderIdAndStoreId(id, storeId).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> searchOrders(String query) {
        if (query == null || query.isBlank()) return List.of();
        UUID storeId = TenantContext.getStoreId();
        return orderRepo.search(storeId, query.trim()).stream().map(this::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public Optional<OrderSummaryDTO> findSummaryById(UUID orderId) {
        UUID storeId = TenantContext.getStoreId();
        return orderRepo.findByOrderIdAndStoreId(orderId, storeId).map(this::toSummaryDTO);
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryDTO> getAllOrderSummaries() {
        UUID storeId = TenantContext.getStoreId();
        List<Shipment> allShipments = shipmentRepo.findAllByStoreId(storeId);
        return orderRepo.findAllByStoreId(storeId).stream()
                .map(order -> {
                    Shipment shipment = allShipments.stream()
                            .filter(s -> s.getOrderId().equals(order.getOrderId()))
                            .findFirst()
                            .orElse(null);
                    return toSummaryDTO(order, shipment);
                })
                .toList();
    }

    private OrderResponseDTO createOnhandOrder(OrderRequestDTO request, Item item) {
        if (item.getStock() < request.quantity()) {
            this.message = "Sold out. This item is only available from on-hand stock.";
            return null;
        }
        item.decreaseStock(request.quantity());
        itemRepo.save(item);

        Order order = buildOrder(request, item, request.quantity(), request.paymentType());
        Order saved = orderRepo.save(order);
        this.message = "Order created: Sold from existing stock.";
        return toResponseDTO(saved);
    }

    private OrderResponseDTO createPreorderOrder(OrderRequestDTO request, Item item) {
        Order order = buildOrder(request, item, request.quantity(), PaymentType.PREORDER);
        Order saved = orderRepo.save(order);
        this.message = "Pre-order accepted.";
        return toResponseDTO(saved);
    }

    private OrderResponseDTO createHybridOrder(OrderRequestDTO request, Item item) {
        if (item.getStock() >= request.quantity()) {
            item.decreaseStock(request.quantity());
            itemRepo.save(item);

            Order order = buildOrder(request, item, request.quantity(), request.paymentType());
            this.message = "Order created: Sold from existing stock.";
            return toResponseDTO(orderRepo.save(order));
        }

        if (item.getStock() > 0) {
            int stockQty = item.getStock();
            int preorderQty = request.quantity() - stockQty;

            item.decreaseStock(stockQty);
            itemRepo.save(item);

            // Split shipping fee proportionally by quantity so neither half double-bills it
            BigDecimal totalFee = request.shippingFee() != null ? request.shippingFee() : BigDecimal.ZERO;
            BigDecimal stockFee = totalFee
                    .multiply(BigDecimal.valueOf(stockQty))
                    .divide(BigDecimal.valueOf(request.quantity()), 2, RoundingMode.HALF_UP);
            BigDecimal preorderFee = totalFee.subtract(stockFee);

            Order stockOrder = buildOrderWithFee(request, item, stockQty, request.paymentType(), stockFee);
            Order preOrder = buildOrderWithFee(request, item, preorderQty, PaymentType.PREORDER, preorderFee);

            stockOrder = orderRepo.save(stockOrder);
            preOrder = orderRepo.save(preOrder);

            this.message = String.format(
                    "Order split: %d from stock (%s), %d pre-ordered (%s)",
                    stockQty, stockOrder.getOrderId(), preorderQty, preOrder.getOrderId()
            );
            return toResponseDTO(stockOrder);
        }

        Order order = buildOrder(request, item, request.quantity(), PaymentType.PREORDER);
        this.message = "Order created: Full pre-order (out of stock).";
        return toResponseDTO(orderRepo.save(order));
    }

    private Order buildOrder(OrderRequestDTO request, Item item, int quantity, PaymentType paymentType) {
        return buildOrderWithFee(request, item, quantity, paymentType, request.shippingFee());
    }

    private Order buildOrderWithFee(OrderRequestDTO request, Item item, int quantity,
                                    PaymentType paymentType, BigDecimal shippingFee) {
        BigDecimal unitPrice = item.getPrice();
        BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal fee = (shippingFee != null) ? shippingFee : BigDecimal.ZERO;
        BigDecimal total = itemTotal.add(fee);

        BigDecimal dpReq;
        if (paymentType == PaymentType.PREORDER) {
            BigDecimal requested = request.dpRequired();
            dpReq = (requested == null || requested.compareTo(BigDecimal.ZERO) == 0)
                    ? total.multiply(DP_RATE).setScale(2, RoundingMode.HALF_UP)
                    : requested;
        } else {
            dpReq = BigDecimal.ZERO;
        }

        return new Order(
                request.customerId(),
                request.itemId(),
                quantity,
                paymentType,
                unitPrice,
                itemTotal,
                fee,
                dpReq
        );
    }

    private boolean isTerminal(OrderStatus s) {
        return s == OrderStatus.CANCELLED || s == OrderStatus.COMPLETED;
    }

    private int rank(OrderStatus s) {
        return switch (s) {
            case RESERVED -> 1;
            case DP_PAID -> 2;
            case FOR_ORDERING -> 3;
            case ORDERED_FROM_SUPPLIER -> 4;
            case ARRIVED -> 5;
            case FULLY_PAID -> 6;
            case PACKED -> 7;
            case SHIPPED -> 8;
            case COMPLETED -> 9;
            case CANCELLED -> 99;
        };
    }

    public OrderResponseDTO toResponseDTO(Order o) {
        return new OrderResponseDTO(
                o.getOrderId().toString(),
                o.getCustomerId().toString(),
                o.getItemId().toString(),
                o.getQuantity(),
                o.getPaymentType(),
                o.getUnitPriceAtOrderTime(),
                o.getItemTotal(),
                o.getShippingFee(),
                o.getTotalAmount(),
                o.getDpRequired(),
                o.getDpPaid(),
                o.getFinalPaid(),
                o.getBalance(),
                o.getStatus(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }

    private OrderSummaryDTO toSummaryDTO(Order o) {
        return toSummaryDTO(o, null);
    }

    private OrderSummaryDTO toSummaryDTO(Order o, Shipment s) {
        UUID storeId = TenantContext.getStoreId();
        String customerName = customerRepo.findByCustomerIdAndStoreId(o.getCustomerId(), storeId)
                .map(c -> c.getFirstName() + " " + c.getLastName())
                .orElse("Unknown Customer");

        String itemName = itemRepo.findByItemIdAndStoreId(o.getItemId(), storeId)
                .map(Item::getName)
                .orElse("Unknown Item");

        return new OrderSummaryDTO(
                o.getOrderId().toString(),
                customerName,
                itemName,
                o.getQuantity(),
                o.getTotalAmount(),
                o.getDpPaid(),
                o.getFinalPaid(),
                o.getBalance(),
                o.getStatus(),
                o.getPaymentType(),
                s != null ? s.getTrackingNumber() : "PENDING",
                s != null ? s.getShipmentStatus() : "NOT SHIPPED"
        );
    }
}