package com.minebuddy.service;

import com.minebuddy.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpiryMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiry-minutes:15}") long accessTokenExpiryMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiryMillis = accessTokenExpiryMinutes * 60 * 1000;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        
        java.util.HashMap<String, Object> claims = new java.util.HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("superAdmin", user.isSuperAdmin());
        
        if (user.getStore() != null) {
            claims.put("storeId", user.getStore().getStoreId().toString());
        }

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpiryMillis)))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long getAccessTokenExpirySeconds() {
        return accessTokenExpiryMillis / 1000;
    }
}