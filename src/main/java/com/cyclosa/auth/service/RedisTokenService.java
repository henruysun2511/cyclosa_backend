package com.cyclosa.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

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

    /**
     * Lưu mã kích hoạt tài khoản nhân viên (TTL mặc định 48 giờ)
     */
    public void saveActivationToken(String token, UUID userId, Duration ttl) {
        String key = ACTIVATION_PREFIX + token;
        redisTemplate.opsForValue().set(key, userId.toString(), ttl);
        log.debug("Lưu activation token cho userId={} với TTL={}s", userId, ttl.getSeconds());
    }

    /**
     * Lấy ID người dùng tương ứng với mã kích hoạt
     */
    public Optional<UUID> getUserIdByActivationToken(String token) {
        String key = ACTIVATION_PREFIX + token;
        String val = redisTemplate.opsForValue().get(key);
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
        redisTemplate.delete(key);
    }

    /**
     * Lưu Refresh Token của người dùng (TTL mặc định 7 ngày)
     */
    public void saveRefreshToken(UUID userId, String refreshToken, Duration ttl) {
        String key = REFRESH_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, ttl);
        log.debug("Lưu refresh token cho userId={} với TTL={}s", userId, ttl.getSeconds());
    }

    /**
     * Kiểm tra tính hợp lệ của Refresh Token
     */
    public boolean validateRefreshToken(UUID userId, String refreshToken) {
        String key = REFRESH_PREFIX + userId;
        String stored = redisTemplate.opsForValue().get(key);
        return stored != null && stored.equals(refreshToken);
    }

    /**
     * Xóa Refresh Token khi đăng xuất hoặc khóa tài khoản
     */
    public void deleteRefreshToken(UUID userId) {
        String key = REFRESH_PREFIX + userId;
        redisTemplate.delete(key);
    }

    /**
     * Đưa Access Token vào danh sách đen (Blacklist) khi đăng xuất
     */
    public void blacklistAccessToken(String accessToken, Duration remainingTtl) {
        if (remainingTtl.isNegative() || remainingTtl.isZero()) {
            return;
        }
        String key = BLACKLIST_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, "revoked", remainingTtl);
        log.debug("Đưa access token vào blacklist với TTL={}s", remainingTtl.getSeconds());
    }

    /**
     * Kiểm tra xem Access Token có nằm trong danh sách đen không
     */
    public boolean isAccessTokenBlacklisted(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;
        Boolean hasKey = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(hasKey);
    }

    /**
     * Lưu mã OTP khôi phục mật khẩu (TTL 15 phút)
     */
    public void savePasswordResetOtp(String email, String otp, Duration ttl) {
        String key = OTP_PREFIX + email.toLowerCase().trim();
        redisTemplate.opsForValue().set(key, otp, ttl);
        log.debug("Lưu OTP reset password cho email={} với TTL={}s", email, ttl.getSeconds());
    }

    /**
     * Lấy mã OTP khôi phục mật khẩu theo email
     */
    public Optional<String> getPasswordResetOtp(String email) {
        String key = OTP_PREFIX + email.toLowerCase().trim();
        String val = redisTemplate.opsForValue().get(key);
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
        redisTemplate.delete(key);
    }

    /**
     * Tăng số lần nhập sai OTP (dùng chống Brute-force, tối đa 5 lần)
     */
    public int incrementOtpAttempts(String email, Duration ttl) {
        String key = OTP_ATTEMPTS_PREFIX + email.toLowerCase().trim();
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, ttl);
        }
        return attempts != null ? attempts.intValue() : 1;
    }

    /**
     * Lấy số lần nhập sai OTP hiện tại
     */
    public int getOtpAttempts(String email) {
        String key = OTP_ATTEMPTS_PREFIX + email.toLowerCase().trim();
        String val = redisTemplate.opsForValue().get(key);
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
        redisTemplate.delete(key);
    }
}
