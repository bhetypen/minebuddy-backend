package com.minebuddy.service;

import com.minebuddy.dto.request.CreateShipmentRequestDTO;
import com.minebuddy.dto.request.UpdateShipmentStatusRequestDTO;
import com.minebuddy.dto.response.ShipmentResponseDTO;
import com.minebuddy.model.Order;
import com.minebuddy.model.Shipment;
import com.minebuddy.model.enums.OrderStatus;
import com.minebuddy.repository.OrderRepository;
import com.minebuddy.repository.ShipmentRepository;
import com.minebuddy.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ShippingService {

    private static final Set<String> VALID_STATUSES = Set.of(
            "PENDING", "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY",
            "DELIVERED", "FAILED", "RETURNED"
    );

    private final OrderRepository orderRepo;
    private final ShipmentRepository shipmentRepo;

    private String message;

    public ShippingService(OrderRepository orderRepo, ShipmentRepository shipmentRepo) {
        this.orderRepo = orderRepo;
        this.shipmentRepo = shipmentRepo;
    }

    public String getMessage() {
        return message;
    }

    @Transactional
    public ShipmentResponseDTO createShipment(CreateShipmentRequestDTO request) {
        Order order = orderRepo.findById(request.orderId()).orElse(null);
        if (order == null) {
            this.message = "Order not found.";
            return null;
        }

        if (order.getStatus() != OrderStatus.FULLY_PAID && order.getStatus() != OrderStatus.PACKED) {
            this.message = "Order must be FULLY_PAID or PACKED before creating a shipment.";
            return null;
        }

        UUID storeId = TenantContext.getStoreId();

        if (shipmentRepo.findByOrderIdAndStoreId(request.orderId(), storeId).isPresent()) {
            this.message = "Shipment already exists for this order.";
            return null;
        }

        Shipment shipment = new Shipment(
                request.orderId(),
                request.carrier(),
                request.trackingNumber(),
                request.shippingFee()
        );

        order.setStatus(OrderStatus.PACKED);
        orderRepo.save(order);

        Shipment saved = shipmentRepo.save(shipment);
        this.message = "Shipment created. Order status is now PACKED.";
        return toResponseDTO(saved);
    }

    @Transactional
    public boolean updateShipmentStatus(UUID shipmentId, UpdateShipmentStatusRequestDTO request) {
        Shipment shipment = shipmentRepo.findById(shipmentId).orElse(null);
        if (shipment == null) {
            this.message = "Shipment not found.";
            return false;
        }

        String newStatus = request.newStatus();
        if (!VALID_STATUSES.contains(newStatus)) {
            this.message = "Invalid shipment status: " + newStatus;
            return false;
        }

        if (request.trackingNumber() != null && !request.trackingNumber().isBlank()) {
            shipment.setTrackingNumber(request.trackingNumber());
        }

        shipment.setShipmentStatus(newStatus);
        shipmentRepo.save(shipment);

        cascadeOrderStatus(shipment.getOrderId(), newStatus);

        this.message = "Shipment status updated to " + newStatus + ".";
        return true;
    }

    @Transactional
    public boolean markDelivered(UUID shipmentId) {
        Shipment shipment = shipmentRepo.findById(shipmentId).orElse(null);
        if (shipment == null) {
            this.message = "Shipment not found.";
            return false;
        }

        shipment.setShipmentStatus("DELIVERED");
        shipmentRepo.save(shipment);

        cascadeOrderStatus(shipment.getOrderId(), "DELIVERED");

        this.message = "Shipment marked as DELIVERED. Order is now COMPLETED.";
        return true;
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponseDTO> listAll() {
        return shipmentRepo.findAll().stream().map(this::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ShipmentResponseDTO> findByOrderId(UUID orderId) {
        UUID storeId = TenantContext.getStoreId();
        return shipmentRepo.findByOrderIdAndStoreId(orderId, storeId)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<ShipmentResponseDTO> findById(UUID shipmentId) {
        return shipmentRepo.findById(shipmentId).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponseDTO> findByCarrier(String carrier) {
        return shipmentRepo.findAll().stream()
                .filter(s -> s.getCarrier().equalsIgnoreCase(carrier))
                .map(this::toResponseDTO)
                .toList();
    }

    private void cascadeOrderStatus(UUID orderId, String shipmentStatus) {
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return;

        switch (shipmentStatus) {
            case "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY" -> {
                if (order.getStatus() != OrderStatus.SHIPPED
                        && order.getStatus() != OrderStatus.COMPLETED) {
                    order.setStatus(OrderStatus.SHIPPED);
                    orderRepo.save(order);
                }
            }
            case "DELIVERED" -> {
                order.setStatus(OrderStatus.COMPLETED);
                orderRepo.save(order);
            }
            case "RETURNED" -> {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepo.save(order);
            }
        }
    }

    private ShipmentResponseDTO toResponseDTO(Shipment s) {
        return new ShipmentResponseDTO(
                s.getShipmentId().toString(),
                s.getOrderId().toString(),
                s.getCarrier(),
                s.getTrackingNumber(),
                s.getShippingFee(),
                s.getShipmentStatus(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
