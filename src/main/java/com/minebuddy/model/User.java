package com.minebuddy.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id", columnDefinition = "CHAR(36)", updatable = false, nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "super_admin", nullable = false)
    private boolean superAdmin;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected User() {}

    public User(String email, String passwordHash, String name) {
        this.userId = UUID.randomUUID();
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.active = false;
        this.superAdmin = false;
    }

    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public boolean isSuperAdmin() { return superAdmin; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setName(String name) { this.name = name; }
    public void setActive(boolean active) { this.active = active; }
}