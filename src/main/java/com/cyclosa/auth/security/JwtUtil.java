package com.cyclosa.auth.security;

import com.cyclosa.common.exception.AppException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiry:86400000}")
    private long accessTokenExpiry;

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

    private String buildToken(UUID userId, String email,
                              Map<String, Object> extraClaims, long expiry) {
        Date now    = new Date();
        Date expire = new Date(now.getTime() + expiry);

        return Jwts.builder()
                .subject(email)
                .id(userId.toString())
                .claims(extraClaims)
                .issuedAt(now)
                .expiration(expire)
                .signWith(getKey())
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getId());
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = parseClaims(token);
        List<String> roles = claims.get("roles", List.class);
        if (roles != null && !roles.isEmpty()) {
            return roles;
        }
        String singleRole = claims.get("role", String.class);
        return singleRole != null ? List.of(singleRole) : List.of();
    }

    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseClaims(token).get("type", String.class));
    }

    public Claims validateAndExtract(String token) {
        try {
            return parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw AppException.tokenExpired();
        } catch (JwtException e) {
            throw AppException.tokenInvalid();
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
