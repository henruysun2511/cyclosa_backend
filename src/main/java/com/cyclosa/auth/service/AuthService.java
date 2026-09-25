package com.cyclosa.auth.service;

import com.cyclosa.role.service.UserRoleService;
import com.cyclosa.auth.dto.request.ChangePasswordRequest;
import com.cyclosa.auth.dto.request.ForgotPasswordRequest;
import com.cyclosa.auth.dto.request.GoogleIdTokenRequest;
import com.cyclosa.auth.dto.request.LoginRequest;
import com.cyclosa.auth.dto.request.RefreshTokenRequest;
import com.cyclosa.auth.dto.request.ResetPasswordRequest;
import com.cyclosa.auth.dto.request.RegisterRequest;
import com.cyclosa.auth.dto.response.TokenResponse;
import com.cyclosa.auth.dto.response.UserInfo;
import com.cyclosa.auth.entity.OAuthAccount;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.mapper.AuthMapper;
import com.cyclosa.auth.repository.OAuthAccountRepository;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.auth.security.JwtUtil;
import com.cyclosa.common.enums.UserStatus;
import com.cyclosa.common.exception.AppException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.cyclosa.auth.dto.request.ActivateAccountRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository              userRepository;
    private final OAuthAccountRepository      oAuthAccountRepository;
    private final UserRoleService             userRoleService; // Calling Service of admin module, not repositories!
    private final PasswordEncoder             passwordEncoder;
    private final JwtUtil                     jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final AuthMapper                  authMapper;
    private final EmailService                emailService;

    @Value("${app.jwt.access-token-expiry:86400000}")
    private long accessTokenExpiry;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    public static final String BLACKLIST_PREFIX          = "blacklist:";
    public static final String REFRESH_TOKEN_PREFIX      = "refresh:";
    public static final String ACTIVATION_TOKEN_PREFIX   = "activation:";
    public static final String USER_ACTIVATION_PREFIX    = "user_activation:";
    public static final String PASSWORD_RESET_PREFIX     = "password_reset:";

    @Transactional
    public TokenResponse activateAccount(ActivateAccountRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw AppException.validationFailed("Mật khẩu xác nhận không khớp");
        }

        String redisKey = ACTIVATION_TOKEN_PREFIX + req.getToken().trim();
        String userIdStr = redisTemplate.opsForValue().get(redisKey);
        if (userIdStr == null) {
            throw AppException.activationTokenInvalid();
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw AppException.activationTokenInvalid();
        }

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> AppException.userNotFound(userId));

        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Invalidate token
        redisTemplate.delete(redisKey);
        redisTemplate.delete(USER_ACTIVATION_PREFIX + userId);

        return generateTokenResponse(user);
    }

    @Deprecated
    @Transactional
    public TokenResponse register(RegisterRequest req) {
        String cleanEmail = req.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw AppException.userEmailExists(cleanEmail);
        }

        User user = User.builder()
                .email(cleanEmail)
                .username(cleanEmail)
                .fullName(req.getFullName().trim())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .status(UserStatus.ACTIVE)
                .version(0)
                .build();

        user = userRepository.save(user);

        // Delegate to UserRoleService in admin module
        userRoleService.assignDefaultRoleToUser(user.getId(), "EMPLOYEE", null);

        return generateTokenResponse(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        String cleanEmail = req.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmailWithRoles(cleanEmail)
                .orElseThrow(AppException::invalidCredentials);

        if (user.getPasswordHash() == null) {
            if (user.getStatus() == UserStatus.PENDING_ACTIVATION) {
                throw AppException.accountPendingActivation();
            }
            throw AppException.invalidCredentials();
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw AppException.invalidCredentials();
        }

        if (user.getStatus() == UserStatus.PENDING_ACTIVATION) {
            throw AppException.accountPendingActivation();
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw AppException.accountLocked();
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw AppException.accountDisabled();
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return generateTokenResponse(user);
    }

    @Transactional
    public TokenResponse oauth2Login(String sub, String email, String name, String picture) {
        OAuthAccount oAuthAccount = oAuthAccountRepository
                .findByProviderAndProviderId("google", sub)
                .orElse(null);

        User user;

        if (oAuthAccount != null) {
            user = oAuthAccount.getUser();
        } else {
            user = userRepository.findByEmailWithRoles(email).orElse(null);

            if (user == null) {
                user = User.builder()
                        .email(email)
                        .username(email)
                        .fullName(name)
                        .avatarUrl(picture)
                        .status(UserStatus.ACTIVE)
                        .version(0)
                        .build();
                user = userRepository.save(user);

                // Delegate to UserRoleService in admin module
                userRoleService.assignDefaultRoleToUser(user.getId(), "EMPLOYEE", null);
            }

            oAuthAccountRepository.save(OAuthAccount.builder()
                    .user(user)
                    .provider("google")
                    .providerId(sub)
                    .email(email)
                    .build());
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw AppException.accountDisabled();
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return generateTokenResponse(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse refreshToken(RefreshTokenRequest req) {
        String refreshToken = req.getRefreshToken();

        boolean isRefresh;
        try {
            isRefresh = jwtUtil.isRefreshToken(refreshToken);
        } catch (Exception e) {
            throw AppException.refreshTokenInvalid();
        }
        if (!isRefresh) {
            throw AppException.refreshTokenInvalid();
        }

        UUID userId = jwtUtil.extractUserId(refreshToken);
        String redisKey = REFRESH_TOKEN_PREFIX + userId;
        String stored   = redisTemplate.opsForValue().get(redisKey);

        if (stored == null || !stored.equals(refreshToken)) {
            throw AppException.refreshTokenInvalid();
        }

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(AppException::unauthorized);

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getRoleCodes());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessTokenExpiry / 1000)
                .tokenType("Bearer")
                .user(authMapper.toUserInfo(user))
                .build();
    }

    public void logout(String accessToken) {
        try {
            UUID userId = jwtUtil.extractUserId(accessToken);
            Date expiry = jwtUtil.extractExpiration(accessToken);
            long ttl    = expiry.getTime() - System.currentTimeMillis();

            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + accessToken,
                        "revoked",
                        ttl, TimeUnit.MILLISECONDS
                );
            }

            redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        } catch (Exception ignored) {
        }
    }

    @Transactional(readOnly = true)
    public UserInfo getCurrentUser(UUID userId) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> AppException.userNotFound(userId));
        return authMapper.toUserInfo(user);
    }

    // ==========================================
    // Forgot Password — Gửi email khôi phục
    // ==========================================
    @Transactional(readOnly = true)
    public void forgotPassword(ForgotPasswordRequest req) {
        String cleanEmail = req.getEmail().toLowerCase().trim();
        userRepository.findByEmail(cleanEmail).ifPresent(user -> {
            if (user.getStatus() != UserStatus.ACTIVE) {
                log.warn("[FORGOT PASSWORD] Tài khoản {} không ở trạng thái ACTIVE, bỏ qua.", cleanEmail);
                return;
            }

            // Xóa token cũ nếu có (chỉ cho phép 1 token valid tại mọi thời điểm)
            String existingTokenKey = PASSWORD_RESET_PREFIX + "user:" + user.getId();
            String oldToken = redisTemplate.opsForValue().get(existingTokenKey);
            if (oldToken != null) {
                redisTemplate.delete(PASSWORD_RESET_PREFIX + oldToken);
            }

            // Tạo token mới
            String resetToken = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(
                    PASSWORD_RESET_PREFIX + resetToken,
                    user.getId().toString(),
                    1, TimeUnit.HOURS
            );
            redisTemplate.opsForValue().set(
                    PASSWORD_RESET_PREFIX + "user:" + user.getId(),
                    resetToken,
                    1, TimeUnit.HOURS
            );

            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken);
        });

        // Luôn trả OK dù email có tồn tại hay không — chống email enumeration attack
        log.info("[FORGOT PASSWORD] Xử lý yêu cầu quên mật khẩu cho email: {}", cleanEmail);
    }

    // ==========================================
    // Reset Password — Đặt lại mật khẩu bằng token
    // ==========================================
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw AppException.validationFailed("Mật khẩu xác nhận không khớp");
        }

        String redisKey = PASSWORD_RESET_PREFIX + req.getToken().trim();
        String userIdStr = redisTemplate.opsForValue().get(redisKey);
        if (userIdStr == null) {
            throw AppException.passwordResetTokenInvalid();
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw AppException.passwordResetTokenInvalid();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.userNotFound(userId));

        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);

        // Xóa token đã sử dụng
        redisTemplate.delete(redisKey);
        redisTemplate.delete(PASSWORD_RESET_PREFIX + "user:" + userId);

        // Force re-login: xóa refresh token hiện tại
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);

        log.info("[RESET PASSWORD] Đặt lại mật khẩu thành công cho User id={}", userId);
    }

    // ==========================================
    // Change Password — Đổi mật khẩu cá nhân
    // ==========================================
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmNewPassword())) {
            throw AppException.validationFailed("Mật khẩu mới xác nhận không khớp");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.userNotFound(userId));

        if (user.getPasswordHash() == null) {
            throw AppException.validationFailed("Tài khoản chưa thiết lập mật khẩu, vui lòng sử dụng chức năng kích hoạt tài khoản");
        }

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw AppException.passwordMismatch();
        }

        if (passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw AppException.validationFailed("Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        // Force re-login: xóa refresh token hiện tại
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);

        log.info("[CHANGE PASSWORD] Đổi mật khẩu thành công cho User id={}", userId);
    }

    // ==========================================
    // Google ID Token Login — REST API cho frontend
    // ==========================================
    @Transactional
    public TokenResponse googleIdTokenLogin(GoogleIdTokenRequest req) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(req.getIdToken());
            if (idToken == null) {
                throw AppException.googleTokenInvalid();
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String sub     = payload.getSubject();
            String email   = payload.getEmail();
            String name    = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            if (email == null) {
                throw AppException.googleTokenInvalid();
            }

            if (name == null) {
                name = email;
            }

            return oauth2Login(sub, email, name, picture);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("[GOOGLE LOGIN] Xác thực Google ID Token thất bại: {}", e.getMessage());
            throw AppException.googleTokenInvalid();
        }
    }

    private TokenResponse generateTokenResponse(User user) {
        List<String> roleCodes = user.getRoleCodes();
        String accessToken  = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), roleCodes);
        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId(), user.getEmail());

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                7, TimeUnit.DAYS
        );

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessTokenExpiry / 1000)
                .tokenType("Bearer")
                .user(authMapper.toUserInfo(user))
                .build();
    }
}
