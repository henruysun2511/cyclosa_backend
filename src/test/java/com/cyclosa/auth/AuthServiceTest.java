package com.cyclosa.auth;

import com.cyclosa.auth.dto.request.*;
import com.cyclosa.auth.dto.response.AuthTokenResponse;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.enums.UserStatus;
import com.cyclosa.auth.exception.AuthErrorCode;
import com.cyclosa.auth.mapper.UserMapper;
import com.cyclosa.auth.repository.OAuthAccountRepository;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.auth.security.JwtUtil;
import com.cyclosa.auth.service.AuthService;
import com.cyclosa.auth.service.MailService;
import com.cyclosa.auth.service.RedisTokenService;
import com.cyclosa.common.exception.AppException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuthAccountRepository oauthAccountRepository;

    @Mock
    private RedisTokenService redisTokenService;

    @Mock
    private MailService mailService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private UUID userId;
    private User activeUser;
    private User pendingUser;
    private User lockedUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        activeUser = User.builder()
                .username("active_user")
                .email("active@cyclosa.com")
                .passwordHash("$2a$10$hashedpassword")
                .status(UserStatus.ACTIVE)
                .deleted(false)
                .build();
        activeUser.setId(userId);

        pendingUser = User.builder()
                .username("pending_user")
                .email("pending@cyclosa.com")
                .status(UserStatus.PENDING_ACTIVATION)
                .deleted(false)
                .build();
        pendingUser.setId(UUID.randomUUID());

        lockedUser = User.builder()
                .username("locked_user")
                .email("locked@cyclosa.com")
                .passwordHash("$2a$10$hashedpassword")
                .status(UserStatus.LOCKED)
                .deleted(false)
                .build();
        lockedUser.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Đăng nhập thành công khi email và mật khẩu hợp lệ")
    void login_whenValidCredentials_shouldReturnTokens() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("active_user")
                .password("Password@123")
                .build();

        when(userRepository.findByUsernameAndDeletedFalse("active_user")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Password@123", activeUser.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateAccessToken(eq(userId), eq("active@cyclosa.com"), any())).thenReturn("mock-access-token");
        when(jwtUtil.generateRefreshToken(eq(userId), eq("active@cyclosa.com"))).thenReturn("mock-refresh-token");
        when(jwtUtil.getAccessTokenExpiry()).thenReturn(86400000L);
        when(jwtUtil.getRefreshTokenExpiry()).thenReturn(604800000L);

        // When
        AuthTokenResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
        assertThat(response.getEmail()).isEqualTo("active@cyclosa.com");
        verify(userRepository, times(1)).save(activeUser);
        verify(redisTokenService, times(1)).saveRefreshToken(eq(userId), eq("mock-refresh-token"), any(Duration.class));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi đăng nhập tài khoản chưa kích hoạt (PENDING_ACTIVATION)")
    void login_whenPendingActivation_shouldThrowException() {
        LoginRequest request = LoginRequest.builder()
                .username("pending_user")
                .password("Password@123")
                .build();

        when(userRepository.findByUsernameAndDeletedFalse("pending_user")).thenReturn(Optional.of(pendingUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.ACCOUNT_PENDING_ACTIVATION);
    }

    @Test
    @DisplayName("Ném ngoại lệ khi đăng nhập tài khoản đang bị khóa (LOCKED)")
    void login_whenLocked_shouldThrowException() {
        LoginRequest request = LoginRequest.builder()
                .username("locked_user")
                .password("Password@123")
                .build();

        when(userRepository.findByUsernameAndDeletedFalse("locked_user")).thenReturn(Optional.of(lockedUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.ACCOUNT_LOCKED);
    }

    @Test
    @DisplayName("Kích hoạt tài khoản thành công khi token 48h trên Redis hợp lệ")
    void activateAccount_whenTokenValid_shouldActivateAndLogin() {
        // Given
        String token = "valid-token-48h";
        ActivateAccountRequest request = ActivateAccountRequest.builder()
                .token(token)
                .newPassword("Password@2026")
                .build();

        when(redisTokenService.getUserIdByActivationToken(token)).thenReturn(Optional.of(userId));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(pendingUser));
        when(passwordEncoder.encode("Password@2026")).thenReturn("$2a$10$encodedpassword");
        when(jwtUtil.generateAccessToken(any(), any(), any())).thenReturn("mock-access-token");
        when(jwtUtil.generateRefreshToken(any(), any())).thenReturn("mock-refresh-token");

        // When
        AuthTokenResponse response = authService.activateAccount(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(pendingUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(redisTokenService, times(1)).deleteActivationToken(token);
        verify(userRepository, times(1)).save(pendingUser);
    }

    @Test
    @DisplayName("Làm mới Access Token thành công khi Refresh Token trong Redis hợp lệ")
    void refreshToken_whenValid_shouldReturnNewAccessToken() {
        // Given
        String refreshToken = "valid-refresh-token";
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.extractUserId(refreshToken)).thenReturn(userId);
        when(redisTokenService.validateRefreshToken(userId, refreshToken)).thenReturn(true);
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(activeUser));
        when(jwtUtil.generateAccessToken(eq(userId), eq("active@cyclosa.com"), any())).thenReturn("new-access-token");

        // When
        AuthTokenResponse response = authService.refreshToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("Đăng xuất thành công: đưa Access Token vào Blacklist và xóa Refresh Token")
    void logout_whenBearerTokenProvided_shouldBlacklistAndRemoveSession() {
        // Given
        String bearer = "Bearer my-access-token";
        when(jwtUtil.extractUserId("my-access-token")).thenReturn(userId);
        when(jwtUtil.getRemainingExpiration("my-access-token")).thenReturn(Duration.ofMinutes(15));

        // When
        authService.logout(bearer);

        // Then
        verify(redisTokenService, times(1)).blacklistAccessToken("my-access-token", Duration.ofMinutes(15));
        verify(redisTokenService, times(1)).deleteRefreshToken(userId);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==========================================
    // Quên mật khẩu qua OTP (Forgot Password)
    // ==========================================
    @Test
    @DisplayName("Quên mật khẩu: Gửi OTP thành công khi tài khoản đang ACTIVE")
    void forgotPassword_whenUserActive_shouldSaveOtpAndSendEmail() {
        // Given
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("active@cyclosa.com")
                .build();
        when(userRepository.findByUsernameAndDeletedFalse("active_user")).thenReturn(Optional.of(activeUser));

        // When
        authService.forgotPassword(request);

        // Then
        verify(redisTokenService, times(1)).savePasswordResetOtp(eq("active@cyclosa.com"), anyString(), eq(Duration.ofMinutes(15)));
        verify(redisTokenService, times(1)).resetOtpAttempts("active@cyclosa.com");
        verify(mailService, times(1)).sendPasswordResetOtpEmail(eq("active@cyclosa.com"), anyString(), eq(Duration.ofMinutes(15)));
    }

    @Test
    @DisplayName("Quên mật khẩu: Không gửi OTP nếu email không tồn tại trong hệ thống (chống dò email)")
    void forgotPassword_whenUserNotFound_shouldNotSendOtp() {
        // Given
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("nonexistent@cyclosa.com")
                .build();
        when(userRepository.findByEmailAndDeletedFalse("nonexistent@cyclosa.com")).thenReturn(Optional.empty());

        // When
        authService.forgotPassword(request);

        // Then
        verify(redisTokenService, never()).savePasswordResetOtp(anyString(), anyString(), any());
        verify(mailService, never()).sendPasswordResetOtpEmail(anyString(), anyString(), any());
    }

    // ==========================================
    // Đặt lại mật khẩu bằng OTP (Reset Password)
    // ==========================================
    @Test
    @DisplayName("Đặt lại mật khẩu: Báo lỗi khi mật khẩu xác nhận không khớp")
    void resetPassword_whenPasswordsMismatch_shouldThrowException() {
        // Given
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .email("active@cyclosa.com")
                .otp("123456")
                .newPassword("NewPass@123")
                .confirmPassword("MismatchPass@123")
                .build();

        // When & Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.PASSWORD_CONFIRM_MISMATCH);
    }

    @Test
    @DisplayName("Đặt lại mật khẩu: Báo lỗi khi mã OTP đã hết hạn hoặc không tồn tại")
    void resetPassword_whenOtpExpired_shouldThrowException() {
        // Given
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .email("active@cyclosa.com")
                .otp("123456")
                .newPassword("NewPass@123")
                .confirmPassword("NewPass@123")
                .build();

        when(redisTokenService.getOtpAttempts("active@cyclosa.com")).thenReturn(0);
        when(redisTokenService.getPasswordResetOtp("active@cyclosa.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.OTP_EXPIRED);
    }

    @Test
    @DisplayName("Đặt lại mật khẩu: Tăng số lần sai và báo lỗi khi nhập sai OTP")
    void resetPassword_whenOtpInvalid_shouldIncrementAttemptsAndThrow() {
        // Given
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .email("active@cyclosa.com")
                .otp("999999")
                .newPassword("NewPass@123")
                .confirmPassword("NewPass@123")
                .build();

        when(redisTokenService.getOtpAttempts("active@cyclosa.com")).thenReturn(0);
        when(redisTokenService.getPasswordResetOtp("active@cyclosa.com")).thenReturn(Optional.of("123456"));
        when(redisTokenService.incrementOtpAttempts("active@cyclosa.com", Duration.ofMinutes(15))).thenReturn(1);

        // When & Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.OTP_INVALID);
    }

    @Test
    @DisplayName("Đặt lại mật khẩu: Thành công khi OTP chính xác, xóa phiên cũ và cập nhật mật khẩu mới")
    void resetPassword_whenValidOtp_shouldUpdatePassword() {
        // Given
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .email("active@cyclosa.com")
                .otp("123456")
                .newPassword("NewPass@123")
                .confirmPassword("NewPass@123")
                .build();

        when(redisTokenService.getOtpAttempts("active@cyclosa.com")).thenReturn(0);
        when(redisTokenService.getPasswordResetOtp("active@cyclosa.com")).thenReturn(Optional.of("123456"));
        when(userRepository.findByUsernameAndDeletedFalse("active_user")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("NewPass@123", activeUser.getPasswordHash())).thenReturn(false);
        when(passwordEncoder.encode("NewPass@123")).thenReturn("$2a$10$newhashedpassword");

        // When
        authService.resetPassword(request);

        // Then
        verify(userRepository, times(1)).save(activeUser);
        assertThat(activeUser.getPasswordHash()).isEqualTo("$2a$10$newhashedpassword");
        verify(redisTokenService, times(1)).deletePasswordResetOtp("active@cyclosa.com");
        verify(redisTokenService, times(1)).resetOtpAttempts("active@cyclosa.com");
        verify(redisTokenService, times(1)).deleteRefreshToken(activeUser.getId());
    }

    // ==========================================
    // Cập nhật mật khẩu cá nhân (Change Password)
    // ==========================================
    @Test
    @DisplayName("Đổi mật khẩu: Báo lỗi khi nhập sai mật khẩu hiện tại")
    void changePassword_whenCurrentPasswordWrong_shouldThrowException() {
        // Given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("WrongOldPass@123")
                .newPassword("NewPass@123")
                .confirmNewPassword("NewPass@123")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getDetails()).thenReturn(userId.toString());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("WrongOldPass@123", activeUser.getPasswordHash())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.CURRENT_PASSWORD_INCORRECT);
    }

    @Test
    @DisplayName("Đổi mật khẩu: Báo lỗi khi mật khẩu mới trùng mật khẩu hiện tại")
    void changePassword_whenNewPasswordSameAsOld_shouldThrowException() {
        // Given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("OldPass@123")
                .newPassword("OldPass@123")
                .confirmNewPassword("OldPass@123")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getDetails()).thenReturn(userId.toString());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("OldPass@123", activeUser.getPasswordHash())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.PASSWORD_SAME_AS_OLD);
    }

    @Test
    @DisplayName("Đổi mật khẩu thành công: Cập nhật hash và xóa Refresh Token để hủy phiên cũ")
    void changePassword_whenValidCredentials_shouldUpdatePasswordAndInvalidateSession() {
        // Given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("OldPass@123")
                .newPassword("NewPass@456")
                .confirmNewPassword("NewPass@456")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getDetails()).thenReturn(userId.toString());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("OldPass@123", activeUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.matches("NewPass@456", activeUser.getPasswordHash())).thenReturn(false);
        when(passwordEncoder.encode("NewPass@456")).thenReturn("$2a$10$updatedhash");

        // When
        authService.changePassword(request);

        // Then
        verify(userRepository, times(1)).save(activeUser);
        assertThat(activeUser.getPasswordHash()).isEqualTo("$2a$10$updatedhash");
        verify(redisTokenService, times(1)).deleteRefreshToken(userId);
    }
}

