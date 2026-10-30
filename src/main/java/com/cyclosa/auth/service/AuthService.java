package com.cyclosa.auth.service;

import com.cyclosa.auth.dto.request.*;
import com.cyclosa.auth.dto.response.AuthTokenResponse;
import com.cyclosa.auth.dto.response.UserProfileResponse;
import com.cyclosa.auth.entity.OAuthAccount;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.enums.OAuthProvider;
import com.cyclosa.auth.enums.UserStatus;
import com.cyclosa.auth.exception.AuthErrorCode;
import com.cyclosa.auth.mapper.UserMapper;
import com.cyclosa.auth.repository.OAuthAccountRepository;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.auth.security.JwtUtil;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.util.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private final OAuthAccountRepository oauthAccountRepository;
    private final RedisTokenService redisTokenService;
    private final MailService mailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Đăng nhập bằng Email và Mật khẩu
     */
    @Transactional
    public AuthTokenResponse login(LoginRequest request) {
        log.info("Xác thực đăng nhập email: {}", request.getEmail());

        // 1. Tìm người dùng theo email
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_CREDENTIALS));

        // 2. Kiểm tra trạng thái tài khoản
        if (user.getStatus() == UserStatus.PENDING_ACTIVATION) {
            log.warn("Tài khoản chưa được kích hoạt: {}", user.getEmail());
            throw new AppException(AuthErrorCode.ACCOUNT_PENDING_ACTIVATION);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            log.warn("Tài khoản đang bị khóa: {}", user.getEmail());
            throw new AppException(AuthErrorCode.ACCOUNT_LOCKED);
        }

        // 3. Đối chiếu mật khẩu bằng BCrypt
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Mật khẩu không chính xác cho email: {}", user.getEmail());
            throw new AppException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 4. Cập nhật thời điểm đăng nhập gần nhất
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 5. Cấp Access Token và Refresh Token
        return issueAuthTokens(user, List.of("EMPLOYEE"));
    }

    /**
     * Kích hoạt tài khoản nhân viên qua mã token 48 giờ và đặt mật khẩu lần đầu
     */
    @Transactional
    public AuthTokenResponse activateAccount(ActivateAccountRequest request) {
        log.info("Xử lý kích hoạt tài khoản bằng token");

        // 1. Kiểm tra token trên Redis
        UUID userId = redisTokenService.getUserIdByActivationToken(request.getToken().trim())
                .orElseThrow(() -> new AppException(AuthErrorCode.ACTIVATION_TOKEN_INVALID));

        // 2. Tìm User
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(AuthErrorCode.ACCOUNT_LOCKED);
        }

        // 3. Đặt mật khẩu và đổi trạng thái ACTIVE
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 4. Xóa token kích hoạt trên Redis để chống tái sử dụng
        redisTokenService.deleteActivationToken(request.getToken().trim());
        log.info("Kích hoạt tài khoản thành công cho user: {}", user.getEmail());

        // 5. Tự động đăng nhập
        return issueAuthTokens(user, List.of("EMPLOYEE"));
    }

    /**
     * Cấp Access Token mới từ Refresh Token hợp lệ
     */
    public AuthTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken().trim();

        // 1. Kiểm tra tính hợp lệ cơ bản của token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new AppException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        UUID userId = jwtUtil.extractUserId(refreshToken);

        // 2. Đối chiếu với Redis
        if (!redisTokenService.validateRefreshToken(userId, refreshToken)) {
            log.warn("Refresh token không khớp hoặc đã bị thu hồi trong Redis cho userId={}", userId);
            throw new AppException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 3. Tìm user
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(AuthErrorCode.ACCOUNT_LOCKED);
        }

        // 4. Cấp lại Access Token mới
        List<String> roles = List.of("EMPLOYEE");
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
     * Đăng xuất: Đưa Access Token vào Redis Blacklist và xóa Refresh Token
     */
    public void logout(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return;
        }

        String token = bearerToken.substring(7).trim();
        try {
            UUID userId = jwtUtil.extractUserId(token);
            Duration remainingTtl = jwtUtil.getRemainingExpiration(token);

            // 1. Đưa Access Token vào Redis Blacklist
            redisTokenService.blacklistAccessToken(token, remainingTtl);

            // 2. Xóa Refresh Token tương ứng
            redisTokenService.deleteRefreshToken(userId);

            log.info("Đăng xuất thành công cho userId={}", userId);
        } catch (Exception e) {
            log.debug("Lỗi khi xử lý logout: {}", e.getMessage());
        }
    }

    /**
     * Đăng nhập / Tự động liên kết bằng Google ID Token
     */
    @Transactional
    public AuthTokenResponse loginWithGoogle(GoogleLoginRequest request) {
        log.info("Xác thực đăng nhập Google ID Token");

        // 1. Xác thực Google Token thông qua endpoint googleapis
        String googleEmail;
        String googleSub;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest verifyRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken().trim()))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(verifyRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Google tokeninfo trả về mã lỗi: {}", response.statusCode());
                throw new AppException(AuthErrorCode.GOOGLE_AUTH_FAILED);
            }

            JsonNode payload = objectMapper.readTree(response.body());
            googleEmail = payload.path("email").asText();
            googleSub = payload.path("sub").asText();

            if (googleEmail == null || googleEmail.isBlank() || googleSub == null || googleSub.isBlank()) {
                throw new AppException(AuthErrorCode.GOOGLE_AUTH_FAILED);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Không thể kết nối dịch vụ Google để xác minh token: {}", e.getMessage());
            throw new AppException(AuthErrorCode.GOOGLE_AUTH_FAILED);
        }

        // 2. Tìm kiếm liên kết oauth_accounts
        Optional<OAuthAccount> oauthOpt = oauthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.GOOGLE, googleSub);

        User user;
        if (oauthOpt.isPresent()) {
            user = oauthOpt.get().getUser();
        } else {
            // Tìm theo email xem đã có tài khoản chưa
            Optional<User> existingUserOpt = userRepository.findByEmailAndDeletedFalse(googleEmail.toLowerCase());
            if (existingUserOpt.isPresent()) {
                user = existingUserOpt.get();
            } else {
                // Tạo mới tài khoản ACTIVE vì đã xác minh qua Google
                user = User.builder()
                        .email(googleEmail.toLowerCase())
                        .status(UserStatus.ACTIVE)
                        .deleted(false)
                        .build();
                user = userRepository.save(user);
            }

            // Tạo liên kết OAuthAccount
            OAuthAccount newOAuth = OAuthAccount.builder()
                    .user(user)
                    .provider(OAuthProvider.GOOGLE)
                    .providerUserId(googleSub)
                    .build();
            oauthAccountRepository.save(newOAuth);
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(AuthErrorCode.ACCOUNT_LOCKED);
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return issueAuthTokens(user, List.of("EMPLOYEE"));
    }

    /**
     * Lấy thông tin tài khoản đang đăng nhập hiện tại
     */
    public UserProfileResponse getCurrentUser() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByIdAndDeletedFalse(currentUserId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        UserProfileResponse profile = userMapper.toProfileResponse(user);
        profile.setRoles(List.of("EMPLOYEE"));
        return profile;
    }

    /**
     * Gửi mã OTP khôi phục mật khẩu qua email (thời hạn 15 phút)
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Xử lý yêu cầu quên mật khẩu cho email: {}", email);

        userRepository.findByEmailAndDeletedFalse(email).ifPresent(user -> {
            if (user.getStatus() != UserStatus.ACTIVE) {
                log.warn("Tài khoản {} không ở trạng thái ACTIVE (status={}), bỏ qua gửi OTP", email, user.getStatus());
                return;
            }

            // Sinh mã OTP 6 chữ số an toàn
            String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
            Duration ttl = Duration.ofMinutes(15);

            // Lưu OTP vào Redis và reset attempts
            redisTokenService.savePasswordResetOtp(email, otp, ttl);
            redisTokenService.resetOtpAttempts(email);

            // Gửi email OTP
            mailService.sendPasswordResetOtpEmail(user.getEmail(), otp, ttl);
        });

        // Luôn trả về thành công chống dò quét email (Email Enumeration Attack)
    }

    /**
     * Xác thực mã OTP và đặt lại mật khẩu mới
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Xử lý đặt lại mật khẩu bằng OTP cho email: {}", email);

        // 1. Kiểm tra xác nhận mật khẩu khớp nhau
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(AuthErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        // 2. Chống Brute-force: Kiểm tra số lần nhập sai
        int attempts = redisTokenService.getOtpAttempts(email);
        if (attempts >= 5) {
            log.warn("Email {} đã vượt quá số lần nhập sai OTP (attempts={})", email, attempts);
            throw new AppException(AuthErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
        }

        // 3. Lấy OTP từ Redis
        String storedOtp = redisTokenService.getPasswordResetOtp(email)
                .orElseThrow(() -> new AppException(AuthErrorCode.OTP_EXPIRED));

        // 4. Đối chiếu OTP
        if (!storedOtp.equals(request.getOtp().trim())) {
            int newAttempts = redisTokenService.incrementOtpAttempts(email, Duration.ofMinutes(15));
            log.warn("Nhập sai OTP cho email: {} (lần thứ {})", email, newAttempts);
            if (newAttempts >= 5) {
                throw new AppException(AuthErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
            }
            throw new AppException(AuthErrorCode.OTP_INVALID);
        }

        // 5. Tìm User
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(AuthErrorCode.ACCOUNT_LOCKED);
        }

        // 6. Kiểm tra mật khẩu mới không trùng với mật khẩu hiện tại
        if (user.getPasswordHash() != null && passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new AppException(AuthErrorCode.PASSWORD_SAME_AS_OLD);
        }

        // 7. Cập nhật mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 8. Dọn dẹp Redis & Cưỡng chế đăng nhập lại
        redisTokenService.deletePasswordResetOtp(email);
        redisTokenService.resetOtpAttempts(email);
        redisTokenService.deleteRefreshToken(user.getId());

        log.info("Đặt lại mật khẩu thành công bằng OTP cho user={}", user.getId());
    }

    /**
     * Cập nhật / Đổi mật khẩu cá nhân cho người dùng đang đăng nhập
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Xử lý đổi mật khẩu cho user ID: {}", currentUserId);

        // 1. Kiểm tra xác nhận mật khẩu mới
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new AppException(AuthErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        // 2. Tìm User
        User user = userRepository.findByIdAndDeletedFalse(currentUserId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        // 3. Kiểm tra xem tài khoản đã có mật khẩu chưa
        if (user.getPasswordHash() == null) {
            throw new AppException(AuthErrorCode.NO_PASSWORD_SET);
        }

        // 4. Kiểm tra mật khẩu hiện tại có đúng không
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AppException(AuthErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        // 5. Kiểm tra mật khẩu mới không được trùng mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new AppException(AuthErrorCode.PASSWORD_SAME_AS_OLD);
        }

        // 6. Băm và lưu mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 7. Cưỡng chế ngắt toàn bộ phiên làm việc cũ
        redisTokenService.deleteRefreshToken(user.getId());

        log.info("Đổi mật khẩu thành công cho user ID: {}", currentUserId);
    }

    private AuthTokenResponse issueAuthTokens(User user, List<String> roles) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        // Lưu Refresh Token trên Redis
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
}
