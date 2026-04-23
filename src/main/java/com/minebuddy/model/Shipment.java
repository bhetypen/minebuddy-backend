package com.minebuddy.model;

import com.minebuddy.security.TenantContext;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @Column(name = "shipment_id", length = 36, nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID shipmentId;

    @Column(name = "store_id", length = 36, nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID storeId;

    @Column(name = "order_id", length = 36, nullable = false, unique = true)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID orderId;

    @Column(nullable = false, length = 100)
    private String carrier;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "shipping_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "shipment_status", nullable = false, length = 20)
    private String shipmentStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Shipment() {}

    public Shipment(UUID orderId, String carrier, String trackingNumber, BigDecimal shippingFee) {
        this.shipmentId = UUID.randomUUID();
        this.orderId = orderId;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.shippingFee = (shippingFee != null) ? shippingFee : BigDecimal.ZERO;
        this.shipmentStatus = "PENDING";
    }

    public UUID getShipmentId()         { return shipmentId; }
    public UUID getStoreId()            { return storeId; }
    public UUID getOrderId()            { return orderId; }

    @PrePersist
    public void prePersist() {
        if (this.storeId == null) {
            this.storeId = TenantContext.getStoreId();
        }
    }

    public String getCarrier()          { return carrier; }
    public String getTrackingNumber()   { return trackingNumber; }
    public BigDecimal getShippingFee()  { return shippingFee; }
    public String getShipmentStatus()   { return shipmentStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setCarrier(String carrier) { this.carrier = carrier; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
    public void setShipmentStatus(String shipmentStatus) { this.shipmentStatus = shipmentStatus; }
}
