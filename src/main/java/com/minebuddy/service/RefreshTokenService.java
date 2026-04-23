package com.minebuddy.service;

import com.minebuddy.model.RefreshToken;
import com.minebuddy.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long refreshExpiryMillis;

    public RefreshTokenService(
            RefreshTokenRepository repo,
            @Value("${app.jwt.refresh-token-expiry-days:7}") long refreshExpiryDays) {
        this.repo = repo;
        this.refreshExpiryMillis = refreshExpiryDays * 24 * 60 * 60 * 1000L;
    }

    public record IssuedToken(String rawToken, RefreshToken entity) {}

    /** Generate and persist a new refresh token for this user. Returns the RAW token (send to client) and entity. */
    @Transactional
    public IssuedToken generateForUser(UUID userId) {
        String rawToken = generateRawToken();
        String hash = hash(rawToken);
        Instant expiresAt = Instant.now().plusMillis(refreshExpiryMillis);

        RefreshToken entity = new RefreshToken(userId, hash, expiresAt);
        RefreshToken saved = repo.save(entity);
        return new IssuedToken(rawToken, saved);
    }

    /**
     * Rotate an existing refresh token.
     * Validates, marks old as used, issues new.
     * REUSE DETECTION: if the presented token was already used, revoke ALL user's tokens (account compromise).
     */
    @Transactional
    public IssuedToken rotate(String presentedRawToken) {
        String hash = hash(presentedRawToken);
        Optional<RefreshToken> opt = repo.findByTokenHash(hash);

        if (opt.isEmpty()) {
            throw new RefreshTokenException("Invalid refresh token");
        }

        RefreshToken token = opt.get();

        // REUSE DETECTION: if token was already used, the attacker is reusing a stolen token
        // → nuke all tokens for this user and force re-login
        if (token.isUsed()) {
            repo.revokeAllActiveForUser(token.getUserId(), Instant.now());
            throw new RefreshTokenException("Token reuse detected. All sessions revoked.");
        }

        if (token.isRevoked()) {
            throw new RefreshTokenException("Refresh token has been revoked");
        }

        if (token.isExpired()) {
            throw new RefreshTokenException("Refresh token has expired");
        }

        // Issue new token
        IssuedToken newTokenPair = generateForUser(token.getUserId());

        // Mark old as used, pointing to new
        token.markUsed(newTokenPair.entity().getTokenId());
        repo.save(token);

        return newTokenPair;
    }

    /** Revoke a specific refresh token (logout flow). */
    @Transactional
    public void revoke(String rawToken) {
        String hash = hash(rawToken);
        repo.findByTokenHash(hash).ifPresent(token -> {
            token.revoke();
            repo.save(token);
        });
    }

    /** Revoke all active refresh tokens for a user (e.g. password change, forced logout). */
    @Transactional
    public void revokeAllForUser(UUID userId) {
        repo.revokeAllActiveForUser(userId, Instant.now());
    }

    private String generateRawToken() {
        byte[] bytes = new byte[48]; // 384 bits
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static class RefreshTokenException extends RuntimeException {
        public RefreshTokenException(String msg) { super(msg); }
    }
}