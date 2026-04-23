package com.minebuddy.model;

import com.minebuddy.model.enums.OrderStatus;
import com.minebuddy.model.enums.PaymentType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "order_id", columnDefinition = "CHAR(36)", updatable = false, nullable = false)
    private UUID orderId;

    @Column(name = "customer_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID customerId;

    @Column(name = "item_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID itemId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 20)
    private PaymentType paymentType;

    @Column(name = "unit_price_at_order_time", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceAtOrderTime;

    // Item subtotal: unitPrice × quantity. Does not include shipping.
    @Column(name = "item_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemTotal;

    // Shipping fee for this order. 0 means free shipping / absorbed in price.
    @Column(name = "shipping_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee;

    // Total charged to buyer: itemTotal + shippingFee
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "dp_required", nullable = false, precision = 10, scale = 2)
    private BigDecimal dpRequired;

    @Column(name = "dp_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal dpPaid;

    @Column(name = "final_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPaid;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Order() {}

    public Order(UUID customerId, UUID itemId, int quantity, PaymentType paymentType,
                 BigDecimal unitPriceAtOrderTime, BigDecimal itemTotal,
                 BigDecimal shippingFee, BigDecimal dpRequired) {
        this.orderId = UUID.randomUUID();
        this.customerId = customerId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.paymentType = paymentType;
        this.unitPriceAtOrderTime = unitPriceAtOrderTime;
        this.itemTotal = itemTotal;
        this.shippingFee = (shippingFee != null) ? shippingFee : BigDecimal.ZERO;
        this.totalAmount = this.itemTotal.add(this.shippingFee);
        this.dpRequired = (dpRequired != null) ? dpRequired : BigDecimal.ZERO;
        this.dpPaid = BigDecimal.ZERO;
        this.finalPaid = BigDecimal.ZERO;
        this.balance = this.totalAmount;
        this.status = OrderStatus.RESERVED;
    }

    /** Recompute totalAmount from itemTotal + shippingFee, then rebase balance. */
    public void recalculateTotals() {
        this.totalAmount = this.itemTotal.add(this.shippingFee);
        recalculateBalance();
    }

    public void recalculateBalance() {
        this.balance = this.totalAmount
                .subtract(this.dpPaid)
                .subtract(this.finalPaid);
    }

    public void addDownPayment(BigDecimal amount) {
        this.dpPaid = this.dpPaid.add(amount);
        recalculateBalance();
    }

    public void addFinalPayment(BigDecimal amount) {
        this.finalPaid = this.finalPaid.add(amount);
        recalculateBalance();
    }

    public BigDecimal getTotalPaid() {
        return this.dpPaid.add(this.finalPaid);
    }

    public UUID getOrderId()                    { return orderId; }
    public UUID getCustomerId()                 { return customerId; }
    public UUID getItemId()                     { return itemId; }
    public int getQuantity()                    { return quantity; }
    public PaymentType getPaymentType()         { return paymentType; }
    public BigDecimal getUnitPriceAtOrderTime() { return unitPriceAtOrderTime; }
    public BigDecimal getItemTotal()            { return itemTotal; }
    public BigDecimal getShippingFee()          { return shippingFee; }
    public BigDecimal getTotalAmount()          { return totalAmount; }
    public BigDecimal getDpRequired()           { return dpRequired; }
    public BigDecimal getDpPaid()               { return dpPaid; }
    public BigDecimal getFinalPaid()            { return finalPaid; }
    public BigDecimal getBalance()              { return balance; }
    public OrderStatus getStatus()              { return status; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }

    public void setItemId(UUID itemId)                              { this.itemId = itemId; }
    public void setQuantity(int quantity)                           { this.quantity = quantity; }
    public void setUnitPriceAtOrderTime(BigDecimal p)               { this.unitPriceAtOrderTime = p; }
    public void setItemTotal(BigDecimal itemTotal)                  { this.itemTotal = itemTotal; recalculateTotals(); }
    public void setShippingFee(BigDecimal shippingFee)              { this.shippingFee = shippingFee; recalculateTotals(); }
    public void setTotalAmount(BigDecimal totalAmount)              { this.totalAmount = totalAmount; recalculateBalance(); }
    public void setDpRequired(BigDecimal dpRequired)                { this.dpRequired = dpRequired; }
    public void setDpPaid(BigDecimal dpPaid)                        { this.dpPaid = dpPaid; recalculateBalance(); }
    public void setFinalPaid(BigDecimal finalPaid)                  { this.finalPaid = finalPaid; recalculateBalance(); }
    public void setBalance(BigDecimal balance)                      { this.balance = balance; }
    public void setStatus(OrderStatus status)                       { this.status = status; }
}