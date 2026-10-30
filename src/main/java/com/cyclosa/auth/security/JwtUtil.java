package com.cyclosa.auth.security;

import com.cyclosa.common.exception.AppException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secret;

    @Getter
    @Value("${app.jwt.access-token-expiry:86400000}")
    private long accessTokenExpiry;

    @Getter
    @Value("${app.jwt.refresh-token-expiry:604800000}")
    private long refreshTokenExpiry;

    public String generateAccessToken(UUID userId, String email, List<String> roles) {
        String primaryRole = (roles != null && !roles.isEmpty()) ? roles.get(0) : "EMPLOYEE";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", primaryRole);
        claims.put("roles", roles != null ? roles : List.of(primaryRole));
        return buildToken(userId, email, claims, accessTokenExpiry);
    }

    public String generateRefreshToken(UUID userId, String email) {
        return buildToken(userId, email, Map.of("type", "refresh"), refreshTokenExpiry);
    }

    private String buildToken(UUID userId, String email, Map<String, Object> extraClaims, long expiry) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expiry);

        return Jwts.builder()
                .subject(email)
                .id(userId.toString())
                .claims(extraClaims)
                .issuedAt(now)
                .expiration(expire)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token đã hết hạn: {}", e.getMessage());
            throw AppException.unauthorized("Token đã hết hạn");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT token không hợp lệ: {}", e.getMessage());
            throw AppException.unauthorized("Token không hợp lệ");
        }
    }

    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        String idStr = claims.getId();
        if (idStr == null) {
            throw AppException.unauthorized("Token thiếu ID người dùng");
        }
        return UUID.fromString(idStr);
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?>) {
            return (List<String>) rolesObj;
        }
        Object singleRole = claims.get("role");
        if (singleRole != null) {
            return List.of(singleRole.toString());
        }
        return List.of("EMPLOYEE");
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Duration getRemainingExpiration(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            long diff = expiration.getTime() - System.currentTimeMillis();
            return Duration.ofMillis(Math.max(diff, 0));
        } catch (Exception e) {
            return Duration.ZERO;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

