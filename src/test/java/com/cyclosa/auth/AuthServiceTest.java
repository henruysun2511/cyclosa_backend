package com.cyclosa.auth;

import com.cyclosa.role.service.UserRoleService;
import com.cyclosa.auth.dto.request.LoginRequest;
import com.cyclosa.auth.dto.request.RegisterRequest;
import com.cyclosa.auth.dto.response.TokenResponse;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.mapper.AuthMapperImpl;
import com.cyclosa.auth.repository.OAuthAccountRepository;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.auth.security.JwtUtil;
import com.cyclosa.auth.service.AuthService;
import com.cyclosa.common.enums.UserStatus;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository              userRepository;
    @Mock OAuthAccountRepository      oAuthAccountRepository;
    @Mock UserRoleService             userRoleService;
    @Mock PasswordEncoder             passwordEncoder;
    @Mock JwtUtil                     jwtUtil;
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOps;

    @Spy AuthMapperImpl authMapper = new AuthMapperImpl();

    @InjectMocks AuthService authService;

    private User activeUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpiry", 86400000L);
        testUserId = UUID.randomUUID();
        activeUser = User.builder()
                .email("test@cyclosa.com")
                .fullName("Test User")
                .passwordHash("$2a$hashed")
                .status(UserStatus.ACTIVE)
                .version(0)
                .build();
        activeUser.setId(testUserId);
    }

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Thành công: tạo user mới và trả về cặp token")
        void success() {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("new@cyclosa.com");
            req.setFullName("New User");
            req.setPassword("Password@123");

            UUID newUserId = UUID.randomUUID();

            given(userRepository.existsByEmail("new@cyclosa.com")).willReturn(false);
            given(passwordEncoder.encode("Password@123")).willReturn("$2a$newhash");
            given(userRepository.save(any(User.class))).willAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(newUserId);
                return u;
            });
            given(jwtUtil.generateAccessToken(eq(newUserId), eq("new@cyclosa.com"), anyList())).willReturn("access-token");
            given(jwtUtil.generateRefreshToken(newUserId, "new@cyclosa.com")).willReturn("refresh-token");
            given(redisTemplate.opsForValue()).willReturn(valueOps);

            TokenResponse res = authService.register(req);

            assertThat(res.getAccessToken()).isEqualTo("access-token");
            assertThat(res.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(res.getUser().getEmail()).isEqualTo("new@cyclosa.com");
            verify(userRepository).save(any(User.class));
            verify(userRoleService).assignDefaultRoleToUser(newUserId, "EMPLOYEE", null);
        }

        @Test
        @DisplayName("Lỗi: email đã tồn tại")
        void emailExists() {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("test@cyclosa.com");
            req.setFullName("Duplicate");
            req.setPassword("Password@123");

            given(userRepository.existsByEmail("test@cyclosa.com")).willReturn(true);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.cyclosa.auth.exception.AuthErrorCode.USER_EMAIL_EXISTS);
        }
    }

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Thành công: thông tin đăng nhập đúng")
        void success() {
            LoginRequest req = new LoginRequest();
            req.setEmail("test@cyclosa.com");
            req.setPassword("Password@123");

            given(userRepository.findByEmailWithRoles("test@cyclosa.com")).willReturn(Optional.of(activeUser));
            given(passwordEncoder.matches("Password@123", "$2a$hashed")).willReturn(true);
            given(jwtUtil.generateAccessToken(eq(testUserId), eq("test@cyclosa.com"), anyList())).willReturn("access-token");
            given(jwtUtil.generateRefreshToken(testUserId, "test@cyclosa.com")).willReturn("refresh-token");
            given(redisTemplate.opsForValue()).willReturn(valueOps);

            TokenResponse res = authService.login(req);

            assertThat(res.getAccessToken()).isEqualTo("access-token");
            assertThat(res.getUser().getId()).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("Lỗi: tài khoản chưa kích hoạt (PENDING_ACTIVATION)")
        void pendingActivation() {
            activeUser.setStatus(UserStatus.PENDING_ACTIVATION);
            LoginRequest req = new LoginRequest();
            req.setEmail("test@cyclosa.com");
            req.setPassword("Password@123");

            given(userRepository.findByEmailWithRoles("test@cyclosa.com")).willReturn(Optional.of(activeUser));
            given(passwordEncoder.matches("Password@123", "$2a$hashed")).willReturn(true);

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.cyclosa.auth.exception.AuthErrorCode.ACCOUNT_PENDING_ACTIVATION);
        }

        @Test
        @DisplayName("Lỗi: tài khoản bị tạm khóa (LOCKED)")
        void accountLocked() {
            activeUser.setStatus(UserStatus.LOCKED);
            LoginRequest req = new LoginRequest();
            req.setEmail("test@cyclosa.com");
            req.setPassword("Password@123");

            given(userRepository.findByEmailWithRoles("test@cyclosa.com")).willReturn(Optional.of(activeUser));
            given(passwordEncoder.matches("Password@123", "$2a$hashed")).willReturn(true);

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.cyclosa.auth.exception.AuthErrorCode.ACCOUNT_LOCKED);
        }
    }

    @Nested
    @DisplayName("activateAccount()")
    class ActivateAccountTests {

        @Test
        @DisplayName("Thành công: token hợp lệ, đặt mật khẩu và kích hoạt tài khoản")
        void success() {
            com.cyclosa.auth.dto.request.ActivateAccountRequest req =
                    new com.cyclosa.auth.dto.request.ActivateAccountRequest("token-123", "NewPass@123", "NewPass@123");

            User pendingUser = User.builder()
                    .email("pending@cyclosa.com")
                    .fullName("Pending User")
                    .status(UserStatus.PENDING_ACTIVATION)
                    .version(0)
                    .build();
            pendingUser.setId(testUserId);

            given(redisTemplate.opsForValue()).willReturn(valueOps);
            given(valueOps.get(AuthService.ACTIVATION_TOKEN_PREFIX + "token-123")).willReturn(testUserId.toString());
            given(userRepository.findByIdWithRoles(testUserId)).willReturn(Optional.of(pendingUser));
            given(passwordEncoder.encode("NewPass@123")).willReturn("$2a$newPassHash");
            given(userRepository.save(any(User.class))).willReturn(pendingUser);
            given(jwtUtil.generateAccessToken(eq(testUserId), eq("pending@cyclosa.com"), anyList())).willReturn("access-token");
            given(jwtUtil.generateRefreshToken(testUserId, "pending@cyclosa.com")).willReturn("refresh-token");

            TokenResponse res = authService.activateAccount(req);

            assertThat(res.getAccessToken()).isEqualTo("access-token");
            assertThat(pendingUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(pendingUser.getPasswordHash()).isEqualTo("$2a$newPassHash");
            verify(redisTemplate).delete(AuthService.ACTIVATION_TOKEN_PREFIX + "token-123");
            verify(redisTemplate).delete(AuthService.USER_ACTIVATION_PREFIX + testUserId);
        }

        @Test
        @DisplayName("Lỗi: mật khẩu xác nhận không khớp")
        void passwordMismatch() {
            com.cyclosa.auth.dto.request.ActivateAccountRequest req =
                    new com.cyclosa.auth.dto.request.ActivateAccountRequest("token-123", "NewPass@123", "Mismatch@123");

            assertThatThrownBy(() -> authService.activateAccount(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.cyclosa.common.exception.CommonErrorCode.VALIDATION_FAILED);
        }

        @Test
        @DisplayName("Lỗi: token không tồn tại hoặc đã hết hạn trong Redis")
        void invalidToken() {
            com.cyclosa.auth.dto.request.ActivateAccountRequest req =
                    new com.cyclosa.auth.dto.request.ActivateAccountRequest("expired-token", "NewPass@123", "NewPass@123");

            given(redisTemplate.opsForValue()).willReturn(valueOps);
            given(valueOps.get(AuthService.ACTIVATION_TOKEN_PREFIX + "expired-token")).willReturn(null);

            assertThatThrownBy(() -> authService.activateAccount(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.cyclosa.auth.exception.AuthErrorCode.ACTIVATION_TOKEN_INVALID);
        }
    }
}
