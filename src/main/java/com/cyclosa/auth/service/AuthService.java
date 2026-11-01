package com.cyclosa.auth.service;

import com.cyclosa.auth.dto.request.*;
import com.cyclosa.auth.dto.response.AuthTokenResponse;
import com.cyclosa.auth.dto.response.UserProfileResponse;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.enums.UserStatus;
import com.cyclosa.auth.exception.AuthErrorCode;
import com.cyclosa.auth.mapper.UserMapper;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.auth.security.JwtUtil;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.authorization.repository.UserRoleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RedisTokenService redisTokenService;
    private final MailService mailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * ÄÄƒng nháº­p báº±ng Email vÃ  Máº­t kháº©u
     */
    @Transactional
    public AuthTokenResponse login(LoginRequest request) {
        log.info("Xác thực đăng nhập username: {}", request.getUsername());

        // 1. TÃ¬m ngÆ°á»i dÃ¹ng theo email
        String identifier = request.getUsername().trim().toLowerCase();
        User user = (identifier.contains("@")
                ? userRepository.findByEmailAndDeletedFalse(identifier)
                : userRepository.findByUsernameAndDeletedFalse(identifier))
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_CREDENTIALS));

        // 2. Kiá»ƒm tra tráº¡ng thÃ¡i tÃ i khoáº£n
        if (user.getStatus() == UserStatus.PENDING_ACTIVATION) {
            log.warn("TÃ i khoáº£n chÆ°a Ä‘Æ°á»£c kÃ­ch hoáº¡t: {}", user.getEmail());
            throw new AppException(AuthErrorCode.ACCOUNT_PENDING_ACTIVATION);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            log.warn("TÃ i khoáº£n Ä‘ang bá»‹ khÃ³a: {}", user.getEmail());
            throw new AppException(AuthErrorCode.ACCOUNT_LOCKED);
        }

        // 3. Äá»‘i chiáº¿u máº­t kháº©u báº±ng BCrypt
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Máº­t kháº©u khÃ´ng chÃ­nh xÃ¡c cho email: {}", user.getEmail());
            throw new AppException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 4. Cáº­p nháº­t thá»i Ä‘iá»ƒm Ä‘Äƒng nháº­p gáº§n nháº¥t
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 5. Cáº¥p Access Token vÃ  Refresh Token
        return issueAuthTokens(user, resolveUserRoles(user.getId()));
    }

    /**
     * KÃ­ch hoáº¡t tÃ i khoáº£n nhÃ¢n viÃªn qua mÃ£ token 48 giá» vÃ  Ä‘áº·t máº­t kháº©u láº§n Ä‘áº§u
     */
    @Transactional
    public AuthTokenResponse activateAccount(ActivateAccountRequest request) {
        log.info("Xá»­ lÃ½ kÃ­ch hoáº¡t tÃ i khoáº£n báº±ng token");

        // 1. Kiá»ƒm tra token trÃªn Redis
        UUID userId = redisTokenService.getUserIdByActivationToken(request.getToken().trim())
                .orElseThrow(() -> new AppException(AuthErrorCode.ACTIVATION_TOKEN_INVALID));

        // 2. TÃ¬m User
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(AuthErrorCode.ACCOUNT_LOCKED);
        }

        // 3. Äáº·t máº­t kháº©u vÃ  Ä‘á»•i tráº¡ng thÃ¡i ACTIVE
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 4. XÃ³a token kÃ­ch hoáº¡t trÃªn Redis Ä‘á»ƒ chá»‘ng tÃ¡i sá»­ dá»¥ng
        redisTokenService.deleteActivationToken(request.getToken().trim());
        log.info("KÃ­ch hoáº¡t tÃ i khoáº£n thÃ nh cÃ´ng cho user: {}", user.getEmail());

        // 5. Tá»± Ä‘á»™ng Ä‘Äƒng nháº­p
        return issueAuthTokens(user, resolveUserRoles(user.getId()));
    }

    /**
     * Cáº¥p Access Token má»›i tá»« Refresh Token há»£p lá»‡
     */
    public AuthTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken().trim();

        // 1. Kiá»ƒm tra tÃ­nh há»£p lá»‡ cÆ¡ báº£n cá»§a token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new AppException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        UUID userId = jwtUtil.extractUserId(refreshToken);

        // 2. Äá»‘i chiáº¿u vá»›i Redis
        if (!redisTokenService.validateRefreshToken(userId, refreshToken)) {
            log.warn("Refresh token khÃ´ng khá»›p hoáº·c Ä‘Ã£ bá»‹ thu há»“i trong Redis cho userId={}", userId);
            throw new AppException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 3. TÃ¬m user
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(AuthErrorCode.ACCOUNT_LOCKED);
        }

        // 4. Cáº¥p láº¡i Access Token má»›i
        List<String> roles = resolveUserRoles(user.getId());
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), roles);

        return AuthTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiry())
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    /**
     * ÄÄƒng xuáº¥t: ÄÆ°a Access Token vÃ o Redis Blacklist vÃ  xÃ³a Refresh Token
     */
    public void logout(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return;
        }

        String token = bearerToken.substring(7).trim();
        try {
            UUID userId = jwtUtil.extractUserId(token);
            Duration remainingTtl = jwtUtil.getRemainingExpiration(token);

            // 1. ÄÆ°a Access Token vÃ o Redis Blacklist
            redisTokenService.blacklistAccessToken(token, remainingTtl);

            // 2. XÃ³a Refresh Token tÆ°Æ¡ng á»©ng
            redisTokenService.deleteRefreshToken(userId);

            log.info("ÄÄƒng xuáº¥t thÃ nh cÃ´ng cho userId={}", userId);
        } catch (Exception e) {
            log.debug("Lá»—i khi xá»­ lÃ½ logout: {}", e.getMessage());
        }
    }

    public UserProfileResponse getCurrentUser() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByIdAndDeletedFalse(currentUserId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        UserProfileResponse profile = userMapper.toProfileResponse(user);
        profile.setRoles(resolveUserRoles(user.getId()));
        return profile;
    }

    /**
     * Cập nhật thông tin cá nhân (Profile) của người dùng đang đăng nhập
     */
    @Transactional
    public UserProfileResponse updateCurrentUser(UpdateProfileRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Xử lý cập nhật thông tin cá nhân cho user ID: {}", currentUserId);

        User user = userRepository.findByIdAndDeletedFalse(currentUserId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        String newUsername = request.getUsername().trim().toLowerCase();
        if (!newUsername.equalsIgnoreCase(user.getUsername())) {
            if (userRepository.existsByUsernameAndDeletedFalse(newUsername)) {
                throw new AppException(AuthErrorCode.USERNAME_ALREADY_EXISTS);
            }
            user.setUsername(newUsername);
        }

        if (request.getAvatarUrl() != null) {
            String trimmedUrl = request.getAvatarUrl().trim();
            user.setAvatarUrl(trimmedUrl.isEmpty() ? null : trimmedUrl);
        }

        userRepository.save(user);
        log.info("Cập nhật thông tin cá nhân thành công cho user ID: {}", currentUserId);

        UserProfileResponse profile = userMapper.toProfileResponse(user);
        profile.setRoles(resolveUserRoles(user.getId()));
        return profile;
    }

    /**
     * Gá»­i mÃ£ OTP khÃ´i phá»¥c máº­t kháº©u qua email (thá»i háº¡n 15 phÃºt)
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Xá»­ lÃ½ yÃªu cáº§u quÃªn máº­t kháº©u cho email: {}", email);

        userRepository.findByEmailAndDeletedFalse(email).ifPresent(user -> {
            if (user.getStatus() != UserStatus.ACTIVE) {
                log.warn("TÃ i khoáº£n {} khÃ´ng á»Ÿ tráº¡ng thÃ¡i ACTIVE (status={}), bá» qua gá»­i OTP", email, user.getStatus());
                return;
            }

            // Sinh mÃ£ OTP 6 chá»¯ sá»‘ an toÃ n
            String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
            Duration ttl = Duration.ofMinutes(15);

            // LÆ°u OTP vÃ o Redis vÃ  reset attempts
            redisTokenService.savePasswordResetOtp(email, otp, ttl);
            redisTokenService.resetOtpAttempts(email);

            // Gá»­i email OTP
            mailService.sendPasswordResetOtpEmail(user.getEmail(), otp, ttl);
        });

        // LuÃ´n tráº£ vá» thÃ nh cÃ´ng chá»‘ng dÃ² quÃ©t email (Email Enumeration Attack)
    }

    /**
     * XÃ¡c thá»±c mÃ£ OTP vÃ  Ä‘áº·t láº¡i máº­t kháº©u má»›i
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Xá»­ lÃ½ Ä‘áº·t láº¡i máº­t kháº©u báº±ng OTP cho email: {}", email);

        // 1. Kiá»ƒm tra xÃ¡c nháº­n máº­t kháº©u khá»›p nhau
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(AuthErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        // 2. Chá»‘ng Brute-force: Kiá»ƒm tra sá»‘ láº§n nháº­p sai
        int attempts = redisTokenService.getOtpAttempts(email);
        if (attempts >= 5) {
            log.warn("Email {} Ä‘Ã£ vÆ°á»£t quÃ¡ sá»‘ láº§n nháº­p sai OTP (attempts={})", email, attempts);
            throw new AppException(AuthErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
        }

        // 3. Láº¥y OTP tá»« Redis
        String storedOtp = redisTokenService.getPasswordResetOtp(email)
                .orElseThrow(() -> new AppException(AuthErrorCode.OTP_EXPIRED));

        // 4. Äá»‘i chiáº¿u OTP
        if (!storedOtp.equals(request.getOtp().trim())) {
            int newAttempts = redisTokenService.incrementOtpAttempts(email, Duration.ofMinutes(15));
            log.warn("Nháº­p sai OTP cho email: {} (láº§n thá»© {})", email, newAttempts);
            if (newAttempts >= 5) {
                throw new AppException(AuthErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
            }
            throw new AppException(AuthErrorCode.OTP_INVALID);
        }

        // 5. TÃ¬m User
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(AuthErrorCode.ACCOUNT_LOCKED);
        }

        // 6. Kiá»ƒm tra máº­t kháº©u má»›i khÃ´ng trÃ¹ng vá»›i máº­t kháº©u hiá»‡n táº¡i
        if (user.getPasswordHash() != null && passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new AppException(AuthErrorCode.PASSWORD_SAME_AS_OLD);
        }

        // 7. Cáº­p nháº­t máº­t kháº©u má»›i
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 8. Dá»n dáº¹p Redis & CÆ°á»¡ng cháº¿ Ä‘Äƒng nháº­p láº¡i
        redisTokenService.deletePasswordResetOtp(email);
        redisTokenService.resetOtpAttempts(email);
        redisTokenService.deleteRefreshToken(user.getId());

        log.info("Äáº·t láº¡i máº­t kháº©u thÃ nh cÃ´ng báº±ng OTP cho user={}", user.getId());
    }

    /**
     * Cáº­p nháº­t / Äá»•i máº­t kháº©u cÃ¡ nhÃ¢n cho ngÆ°á»i dÃ¹ng Ä‘ang Ä‘Äƒng nháº­p
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Xá»­ lÃ½ Ä‘á»•i máº­t kháº©u cho user ID: {}", currentUserId);

        // 1. Kiá»ƒm tra xÃ¡c nháº­n máº­t kháº©u má»›i
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new AppException(AuthErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        // 2. TÃ¬m User
        User user = userRepository.findByIdAndDeletedFalse(currentUserId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        // 3. Kiá»ƒm tra xem tÃ i khoáº£n Ä‘Ã£ cÃ³ máº­t kháº©u chÆ°a
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AppException(AuthErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        // 5. Kiá»ƒm tra máº­t kháº©u má»›i khÃ´ng Ä‘Æ°á»£c trÃ¹ng máº­t kháº©u cÅ©
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new AppException(AuthErrorCode.PASSWORD_SAME_AS_OLD);
        }

        // 6. BÄƒm vÃ  lÆ°u máº­t kháº©u má»›i
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 7. CÆ°á»¡ng cháº¿ ngáº¯t toÃ n bá»™ phiÃªn lÃ m viá»‡c cÅ©
        redisTokenService.deleteRefreshToken(user.getId());

        log.info("Äá»•i máº­t kháº©u thÃ nh cÃ´ng cho user ID: {}", currentUserId);
    }

    private AuthTokenResponse issueAuthTokens(User user, List<String> roles) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        // LÆ°u Refresh Token trÃªn Redis
        Duration refreshTtl = Duration.ofMillis(jwtUtil.getRefreshTokenExpiry());
        redisTokenService.saveRefreshToken(user.getId(), refreshToken, refreshTtl);

        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiry())
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    private List<String> resolveUserRoles(UUID userId) {
        List<String> roles = userRoleAssignmentRepository.findAllWithRoleByUserId(userId).stream()
                .map(a -> a.getRole().getCode())
                .distinct()
                .toList();
        if (roles.isEmpty()) {
            return List.of("EMPLOYEE");
        }
        return roles;
    }
}

