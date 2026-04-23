package com.minebuddy.repository;

import com.minebuddy.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    List<Shipment> findAllByStoreId(UUID storeId);

    Optional<Shipment> findByOrderIdAndStoreId(UUID orderId, UUID storeId);
    
    Optional<Shipment> findByShipmentIdAndStoreId(UUID shipmentId, UUID storeId);
}
