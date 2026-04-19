package com.minebuddy.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @Column(name = "payment_id", columnDefinition = "CHAR(36)", updatable = false, nullable = false)
    private UUID paymentId;

    @Column(name = "order_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID orderId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @CreationTimestamp
    @Column(name = "payment_date", nullable = false, updatable = false)
    private LocalDateTime paymentDate;

    @UpdateTimestamp
    @Column(name = "payment_updated_date", nullable = false)
    private LocalDateTime paymentUpdatedDate;

    protected Payment() {}

    public Payment(UUID orderId, BigDecimal amount, String paymentMethod,
                   String paymentReference, String receiptUrl) {
        this.paymentId = UUID.randomUUID();
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        this.receiptUrl = receiptUrl;
    }

    // Getters
    public UUID getPaymentId() { return paymentId; }
    public UUID getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public String getReceiptUrl() { return receiptUrl; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public LocalDateTime getPaymentUpdatedDate() { return paymentUpdatedDate; }
}
