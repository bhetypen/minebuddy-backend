package com.minebuddy.model;

import com.minebuddy.security.TenantContext;
import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "customer_id", nullable = false, updatable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID customerId;

    @Column(name = "store_id", length = 36, nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID storeId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "handle")
    private String handle; //Tiktok/Facebook

    @Column(name = "platform")
    private String platform;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address_id", length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID addressId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Customer() {
    }

    public Customer(String firstName, String lastName, String handle,
                    String platform, String phone, UUID addressId,
                    LocalDateTime createdAt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.handle = handle;
        this.platform = platform;
        this.phone = phone;
        this.addressId = addressId;
        this.createdAt = createdAt;
    }


    public String getFullName() {
        return firstName + " " + lastName;
    }

    public UUID getCustomerId() {return customerId;}

    public UUID getStoreId() { return storeId; }

    public String getFirstName() { return firstName;}

    @PrePersist
    public void prePersist() {
        if (this.storeId == null) {
            this.storeId = TenantContext.getStoreId();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public String getLastName() {
        return lastName;
    }

    public String getPlatform() {
        return platform;
    }

    public String getHandle() {
        return handle;
    }

    public String getPhoneNumber() {
        return phone;
    }

    public UUID getAddressId() {
        return addressId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }




    @Override
    public String toString() {
        return customerId + " | " + firstName + " | " + lastName  + " | " + platform + "@" + handle + " | " + phone;
    }


}
