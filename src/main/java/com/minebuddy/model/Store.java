package com.minebuddy.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stores")
public class Store {

    @Id
    @UuidGenerator
    @Column(name = "store_id", length = 36, nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID storeId;

    @Column(nullable = false)
    private String name;

    @Column(name = "facebook_link", length = 500)
    private String facebookLink;

    @Column(name = "instagram_link", length = 500)
    private String instagramLink;

    @Column(name = "tiktok_link", length = 500)
    private String tiktokLink;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "gcash_number", length = 20)
    private String gcashNumber;

    @Column(name = "maya_number", length = 20)
    private String mayaNumber;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "address_id")
    private Address address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Store() {}

    // Getters and Setters
    public UUID getStoreId() { return storeId; }
    public void setStoreId(UUID storeId) { this.storeId = storeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFacebookLink() { return facebookLink; }
    public void setFacebookLink(String facebookLink) { this.facebookLink = facebookLink; }

    public String getInstagramLink() { return instagramLink; }
    public void setInstagramLink(String instagramLink) { this.instagramLink = instagramLink; }

    public String getTiktokLink() { return tiktokLink; }
    public void setTiktokLink(String tiktokLink) { this.tiktokLink = tiktokLink; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getGcashNumber() { return gcashNumber; }
    public void setGcashNumber(String gcashNumber) { this.gcashNumber = gcashNumber; }

    public String getMayaNumber() { return mayaNumber; }
    public void setMayaNumber(String mayaNumber) { this.mayaNumber = mayaNumber; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}