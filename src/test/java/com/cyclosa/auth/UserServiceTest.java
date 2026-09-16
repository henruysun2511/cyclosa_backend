package com.cyclosa.auth;

import com.cyclosa.auth.dto.request.CreateUserRequest;
import com.cyclosa.auth.dto.request.UserFilter;
import com.cyclosa.auth.dto.response.UserResponse;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.event.UserCreatedEvent;
import com.cyclosa.auth.mapper.AuthMapper;
import com.cyclosa.auth.mapper.AuthMapperImpl;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.auth.service.EmailService;
import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.enums.UserStatus;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.exception.ErrorCode;
import com.cyclosa.common.response.PageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;
    @Mock EmailService emailService;

    @Spy AuthMapper authMapper = new AuthMapperImpl();

    @InjectMocks UserService userService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .email("user@cyclosa.com")
                .fullName("Nguyễn Văn User")
                .status(UserStatus.ACTIVE)
                .build();
        testUser.setId(testUserId);
    }

    @Nested
    @DisplayName("getUsers()")
    class GetUsersTests {

        @Test
        @DisplayName("Thành công: tìm kiếm phân trang người dùng")
        void success() {
            UserFilter filter = new UserFilter();
            filter.setKeyword("cyclosa");
            filter.setStatus(UserStatus.ACTIVE);
            filter.setPage(0);
            filter.setSize(10);

            Page<User> page = new PageImpl<>(List.of(testUser));
            given(userRepository.searchUsers(eq("cyclosa"), eq(UserStatus.ACTIVE), any(Pageable.class)))
                    .willReturn(page);

            PageData<UserResponse> result = userService.getUsers(filter);

            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().get(0).getEmail()).isEqualTo("user@cyclosa.com");
        }
    }

    @Nested
    @DisplayName("createUser()")
    class CreateUserTests {

        @Test
        @DisplayName("Thành công: tạo user và phát UserCreatedEvent")
        void success() {
            CreateUserRequest req = CreateUserRequest.builder()
                    .email("new@cyclosa.com")
                    .fullName("New User")
                    .roleCodes(List.of("EMPLOYEE"))
                    .build();

            given(userRepository.existsByEmail("new@cyclosa.com")).willReturn(false);
            given(userRepository.save(any(User.class))).willAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(testUserId);
                return u;
            });
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            UserResponse res = userService.createUser(req);

            assertThat(res.getId()).isEqualTo(testUserId);
            assertThat(res.getStatus()).isEqualTo(UserStatus.PENDING_ACTIVATION);
            verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
            verify(emailService).sendActivationEmail(eq("new@cyclosa.com"), eq("New User"), any());
        }

        @Test
        @DisplayName("Lỗi: email đã tồn tại")
        void emailExists() {
            CreateUserRequest req = CreateUserRequest.builder()
                    .email("user@cyclosa.com")
                    .fullName("New User")
                    .build();

            given(userRepository.existsByEmail("user@cyclosa.com")).willReturn(true);

            assertThatThrownBy(() -> userService.createUser(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", com.cyclosa.auth.exception.AuthErrorCode.USER_EMAIL_EXISTS);
        }
    }

    @Nested
    @DisplayName("lockUser() & unlockUser()")
    class LockUnlockTests {

        @Test
        @DisplayName("Thành công: khóa tài khoản")
        void lockSuccess() {
            given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
            given(userRepository.save(any(User.class))).willReturn(testUser);

            UserResponse res = userService.lockUser(testUserId);

            assertThat(res.getStatus()).isEqualTo(UserStatus.LOCKED);
            verify(redisTemplate).delete("refresh:" + testUserId);
        }

        @Test
        @DisplayName("Thành công: mở khóa tài khoản")
        void unlockSuccess() {
            testUser.setStatus(UserStatus.LOCKED);
            testUser.setPasswordHash("$2a$hash");
            given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
            given(userRepository.save(any(User.class))).willReturn(testUser);

            UserResponse res = userService.unlockUser(testUserId);

            assertThat(res.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }
}
