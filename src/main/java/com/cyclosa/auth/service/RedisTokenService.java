package com.cyclosa.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private static final String ACTIVATION_PREFIX = "auth:activation:";
    private static final String REFRESH_PREFIX = "auth:refresh:";
    private static final String BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String OTP_PREFIX = "auth:otp:";
    private static final String OTP_ATTEMPTS_PREFIX = "auth:otp_attempts:";

    private final StringRedisTemplate redisTemplate;

    // Graceful fallback cache khi Redis offline / không khả dụng ở local dev
    private final ConcurrentHashMap<String, String> localFallbackCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> localExpiryCache = new ConcurrentHashMap<>();

    private void putLocal(String key, String value, Duration ttl) {
        localFallbackCache.put(key, value);
        if (ttl != null) {
            localExpiryCache.put(key, System.currentTimeMillis() + ttl.toMillis());
        }
    }

    private String getLocal(String key) {
        Long expiry = localExpiryCache.get(key);
        if (expiry != null && System.currentTimeMillis() > expiry) {
            localFallbackCache.remove(key);
            localExpiryCache.remove(key);
            return null;
        }
        return localFallbackCache.get(key);
    }

    private void removeLocal(String key) {
        localFallbackCache.remove(key);
        localExpiryCache.remove(key);
    }

    /**
     * Lưu mã kích hoạt tài khoản nhân viên (TTL mặc định 48 giờ)
     */
    public void saveActivationToken(String token, UUID userId, Duration ttl) {
        String key = ACTIVATION_PREFIX + token;
        try {
            redisTemplate.opsForValue().set(key, userId.toString(), ttl);
            log.debug("Lưu activation token cho userId={} với TTL={}s", userId, ttl.getSeconds());
        } catch (Exception e) {
            log.warn("Redis không khả dụng, lưu activation token vào bộ nhớ tạm: {}", e.getMessage());
            putLocal(key, userId.toString(), ttl);
        }
    }

    /**
     * Lấy ID người dùng tương ứng với mã kích hoạt
     */
    public Optional<UUID> getUserIdByActivationToken(String token) {
        String key = ACTIVATION_PREFIX + token;
        String val = null;
        try {
            val = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis không khả dụng, đọc activation token từ bộ nhớ tạm");
        }
        if (val == null || val.isBlank()) {
            val = getLocal(key);
        }
        if (val == null || val.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(val));
        } catch (IllegalArgumentException e) {
            log.warn("Giá trị activation token không hợp lệ: {}", val);
            return Optional.empty();
        }
    }

    /**
     * Xóa mã kích hoạt sau khi đã đặt mật khẩu thành công
     */
    public void deleteActivationToken(String token) {
        String key = ACTIVATION_PREFIX + token;
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
        }
        removeLocal(key);
    }

    /**
     * Lưu Refresh Token của người dùng (TTL mặc định 7 ngày)
     */
    public void saveRefreshToken(UUID userId, String refreshToken, Duration ttl) {
        String key = REFRESH_PREFIX + userId;
        try {
            redisTemplate.opsForValue().set(key, refreshToken, ttl);
            log.debug("Lưu refresh token cho userId={} với TTL={}s", userId, ttl.getSeconds());
        } catch (Exception e) {
            log.warn("Redis không khả dụng, lưu refresh token vào bộ nhớ tạm cho userId={}: {}", userId, e.getMessage());
            putLocal(key, refreshToken, ttl);
        }
    }

    /**
     * Kiểm tra tính hợp lệ của Refresh Token
     */
    public boolean validateRefreshToken(UUID userId, String refreshToken) {
        String key = REFRESH_PREFIX + userId;
        String stored = null;
        try {
            stored = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis không khả dụng, đọc refresh token từ bộ nhớ tạm cho userId={}", userId);
        }
        if (stored == null) {
            stored = getLocal(key);
        }
        return stored != null && stored.equals(refreshToken);
    }

    /**
     * Xóa Refresh Token khi đăng xuất hoặc khóa tài khoản
     */
    public void deleteRefreshToken(UUID userId) {
        String key = REFRESH_PREFIX + userId;
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
        }
        removeLocal(key);
    }

    /**
     * Đưa Access Token vào danh sách đen (Blacklist) khi đăng xuất
     */
    public void blacklistAccessToken(String accessToken, Duration remainingTtl) {
        if (remainingTtl.isNegative() || remainingTtl.isZero()) {
            return;
        }
        String key = BLACKLIST_PREFIX + accessToken;
        try {
            redisTemplate.opsForValue().set(key, "revoked", remainingTtl);
            log.debug("Đưa access token vào blacklist với TTL={}s", remainingTtl.getSeconds());
        } catch (Exception e) {
            log.warn("Redis không khả dụng, đưa access token vào bộ nhớ tạm blacklist: {}", e.getMessage());
            putLocal(key, "revoked", remainingTtl);
        }
    }

    /**
     * Kiểm tra xem Access Token có nằm trong danh sách đen không
     */
    public boolean isAccessTokenBlacklisted(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;
        try {
            Boolean hasKey = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(hasKey)) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return getLocal(key) != null;
    }

    /**
     * Lưu mã OTP khôi phục mật khẩu (TTL 15 phút)
     */
    public void savePasswordResetOtp(String email, String otp, Duration ttl) {
        String key = OTP_PREFIX + email.toLowerCase().trim();
        try {
            redisTemplate.opsForValue().set(key, otp, ttl);
            log.debug("Lưu OTP reset password cho email={} với TTL={}s", email, ttl.getSeconds());
        } catch (Exception e) {
            log.warn("Redis không khả dụng, lưu OTP vào bộ nhớ tạm: {}", e.getMessage());
            putLocal(key, otp, ttl);
        }
    }

    /**
     * Lấy mã OTP khôi phục mật khẩu theo email
     */
    public Optional<String> getPasswordResetOtp(String email) {
        String key = OTP_PREFIX + email.toLowerCase().trim();
        String val = null;
        try {
            val = redisTemplate.opsForValue().get(key);
        } catch (Exception ignored) {
        }
        if (val == null || val.isBlank()) {
            val = getLocal(key);
        }
        if (val == null || val.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(val);
    }

    /**
     * Xóa mã OTP khôi phục mật khẩu sau khi sử dụng
     */
    public void deletePasswordResetOtp(String email) {
        String key = OTP_PREFIX + email.toLowerCase().trim();
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
        }
        removeLocal(key);
    }

    /**
     * Tăng số lần nhập sai OTP (dùng chống Brute-force, tối đa 5 lần)
     */
    public int incrementOtpAttempts(String email, Duration ttl) {
        String key = OTP_ATTEMPTS_PREFIX + email.toLowerCase().trim();
        try {
            Long attempts = redisTemplate.opsForValue().increment(key);
            if (attempts != null && attempts == 1) {
                redisTemplate.expire(key, ttl);
            }
            return attempts != null ? attempts.intValue() : 1;
        } catch (Exception e) {
            log.warn("Redis không khả dụng, đếm số lần OTP sai trong bộ nhớ tạm");
            String cur = getLocal(key);
            int next = cur == null ? 1 : Integer.parseInt(cur) + 1;
            putLocal(key, String.valueOf(next), ttl);
            return next;
        }
    }

    /**
     * Lấy số lần nhập sai OTP hiện tại
     */
    public int getOtpAttempts(String email) {
        String key = OTP_ATTEMPTS_PREFIX + email.toLowerCase().trim();
        String val = null;
        try {
            val = redisTemplate.opsForValue().get(key);
        } catch (Exception ignored) {
        }
        if (val == null || val.isBlank()) {
            val = getLocal(key);
        }
        if (val == null || val.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Đặt lại (xóa) số lần nhập sai OTP
     */
    public void resetOtpAttempts(String email) {
        String key = OTP_ATTEMPTS_PREFIX + email.toLowerCase().trim();
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
        }
        removeLocal(key);
    }
}
