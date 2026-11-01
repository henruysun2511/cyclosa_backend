package com.cyclosa.auth;

import com.cyclosa.auth.dto.request.CreateUserRequest;
import com.cyclosa.auth.dto.request.UserFilterRequest;
import com.cyclosa.auth.dto.response.UserResponse;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.enums.UserStatus;
import com.cyclosa.auth.exception.AuthErrorCode;
import com.cyclosa.auth.mapper.UserMapper;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.auth.service.MailService;
import com.cyclosa.auth.service.RedisTokenService;
import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTokenService redisTokenService;

    @Mock
    private MailService mailService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User sampleUser;
    private UserResponse sampleUserResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        sampleUser = User.builder()
                .username("nhanvien")
                .email("nhanvien@cyclosa.com")
                .status(UserStatus.PENDING_ACTIVATION)
                .deleted(false)
                .build();
        sampleUser.setId(userId);

        sampleUserResponse = UserResponse.builder()
                .id(userId)
                .username("nhanvien")
                .email("nhanvien@cyclosa.com")
                .status(UserStatus.PENDING_ACTIVATION)
                .build();
    }

    @Test
    @DisplayName("Admin tạo tài khoản nhân viên mới thành công và bắn mail kích hoạt 48h")
    void createUser_whenEmailNotExists_shouldSaveAndSendMail() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("nhanvien")
                .email("nhanvien@cyclosa.com")
                .build();

        when(userRepository.existsByUsernameAndDeletedFalse("nhanvien")).thenReturn(false);
        when(userRepository.existsByEmailAndDeletedFalse("nhanvien@cyclosa.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

        // When
        UserResponse result = userService.createUser(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("nhanvien@cyclosa.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(redisTokenService, times(1)).saveActivationToken(anyString(), eq(userId), eq(Duration.ofHours(48)));
        verify(mailService, times(1)).sendActivationEmail(eq("nhanvien@cyclosa.com"), anyString(), eq(Duration.ofHours(48)));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi tạo tài khoản trùng email đã tồn tại")
    void createUser_whenEmailExists_shouldThrowException() {
        CreateUserRequest request = CreateUserRequest.builder()
                .email("nhanvien@cyclosa.com")
                .build();

        when(userRepository.existsByEmailAndDeletedFalse("nhanvien@cyclosa.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Lấy danh sách người dùng có phân trang thành công")
    void getUsers_shouldReturnPagedData() {
        // Given
        UserFilterRequest filter = new UserFilterRequest();
        filter.setPage(0);
        filter.setSize(10);

        Page<User> userPage = new PageImpl<>(List.of(sampleUser));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        UserResponse responseDto = UserResponse.builder()
                .id(userId)
                .email("nhanvien@cyclosa.com")
                .status(UserStatus.PENDING_ACTIVATION)
                .build();
        when(userMapper.toResponse(sampleUser)).thenReturn(responseDto);

        // When
        PageData<UserResponse> result = userService.getUsers(filter);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getEmail()).isEqualTo("nhanvien@cyclosa.com");
    }

    @Test
    @DisplayName("Khóa tài khoản thành công: đổi status = LOCKED và xóa Refresh Token trên Redis")
    void lockUser_shouldUpdateStatusAndRemoveSession() {
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(sampleUser));

        userService.lockUser(userId);

        assertThat(sampleUser.getStatus()).isEqualTo(UserStatus.LOCKED);
        verify(userRepository, times(1)).save(sampleUser);
        verify(redisTokenService, times(1)).deleteRefreshToken(userId);
    }
}

