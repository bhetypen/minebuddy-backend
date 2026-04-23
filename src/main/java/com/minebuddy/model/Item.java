package com.minebuddy.model;

import com.minebuddy.model.enums.SaleType;
import com.minebuddy.security.TenantContext;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @Column(name = "item_id", columnDefinition = "CHAR(36)", updatable = false, nullable = false)
    private UUID itemId;

    @Column(name = "store_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private String name;

    // Short nickname used during Facebook Live streams (e.g. "A1" for "Ariel 1.5kg")
    // Optional — sellers may not assign one to every item.
    @Column(name = "live_name", length = 50)
    private String liveName;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", nullable = false, length = 20)
    private SaleType saleType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Item() {}

    public Item(String name, String category, BigDecimal price, int stock, SaleType saleType) {
        this.itemId = UUID.randomUUID();
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.active = true;
        this.saleType = (saleType != null) ? saleType : SaleType.ONHAND_ONLY;
    }

    public void decreaseStock(int quantity) {
        if (quantity > this.stock) {
            throw new IllegalStateException("Insufficient stock for item " + itemId);
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

    @PrePersist
    public void prePersist() {
        if (this.storeId == null) {
            this.storeId = TenantContext.getStoreId();
        }
    }

    // Getters
    public UUID getItemId()             { return itemId; }
    public UUID getStoreId()            { return storeId; }
    public String getName()             { return name; }
    public String getLiveName()         { return liveName; }
    public String getCategory()         { return category; }
    public BigDecimal getPrice()        { return price; }
    public int getStock()               { return stock; }
    public boolean isActive()           { return active; }
    public SaleType getSaleType()       { return saleType; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setLiveName(String liveName) { this.liveName = liveName; }
    public void setStock(int stock) { this.stock = stock; }
    public void setActive(boolean active) { this.active = active; }
}