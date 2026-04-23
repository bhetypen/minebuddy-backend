package com.minebuddy.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_hash", columnList = "token_hash"),
        @Index(name = "idx_refresh_user_id", columnList = "user_id")
})
public class RefreshToken {

    @Id
    @Column(name = "token_id", columnDefinition = "CHAR(36)", updatable = false, nullable = false)
    private UUID tokenId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID userId;

    // SHA-256 hash of the raw token. Raw token is never persisted.
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // When this token was used to produce a new refresh (null = not yet used)
    @Column(name = "used_at")
    private Instant usedAt;

    // Explicitly revoked (logout, security event)
    @Column(name = "revoked_at")
    private Instant revokedAt;

    // If this token was rotated, the token_id of its replacement — for reuse detection chains
    @Column(name = "replaced_by", columnDefinition = "CHAR(36)")
    private UUID replacedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected RefreshToken() {}

    public RefreshToken(UUID userId, String tokenHash, Instant expiresAt) {
        this.tokenId = UUID.randomUUID();
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isActive() {
        return !isExpired() && !isUsed() && !isRevoked();
    }

    public UUID getTokenId() { return tokenId; }
    public UUID getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public UUID getReplacedBy() { return replacedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void markUsed(UUID replacementTokenId) {
        this.usedAt = Instant.now();
        this.replacedBy = replacementTokenId;
    }

    public void revoke() {
        this.revokedAt = Instant.now();
    }
}