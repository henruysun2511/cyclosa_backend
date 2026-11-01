package com.cyclosa.auth.service;

import com.cyclosa.auth.dto.request.BulkCreateUserItem;
import com.cyclosa.auth.dto.request.BulkCreateUserRequest;
import com.cyclosa.auth.dto.request.CreateUserRequest;
import com.cyclosa.auth.dto.response.BulkCreateUserResponse;
import com.cyclosa.auth.dto.response.BulkUserErrorItem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.cyclosa.auth.dto.request.UserFilterRequest;
import com.cyclosa.auth.dto.response.UserResponse;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.enums.UserStatus;
import com.cyclosa.auth.exception.AuthErrorCode;
import com.cyclosa.auth.mapper.UserMapper;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.auth.repository.specification.UserSpecification;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final Duration ACTIVATION_TOKEN_TTL = Duration.ofHours(48);

    private final UserRepository userRepository;
    private final RedisTokenService redisTokenService;
    private final MailService mailService;
    private final UserMapper userMapper;

    /**
     * Quản trị viên khởi tạo tài khoản nhân viên mới (PENDING_ACTIVATION) và gửi mail 48h
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Admin tạo tài khoản nhân viên mới: {}", email);

        // 1. Kiểm tra trùng lặp email
        if (userRepository.existsByEmailAndDeletedFalse(email)) {
            throw new AppException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 2. Xác định Username (lấy từ request hoặc tự sinh từ prefix email)
        String username;
        if (request.getUsername() != null && !request.getUsername().trim().isBlank()) {
            username = request.getUsername().trim().toLowerCase();
            if (userRepository.existsByUsernameAndDeletedFalse(username)) {
                throw new AppException(AuthErrorCode.USERNAME_ALREADY_EXISTS);
            }
        } else {
            username = email.split("@")[0].toLowerCase();
            if (userRepository.existsByUsernameAndDeletedFalse(username)) {
                username = username + "_" + (System.currentTimeMillis() % 10000);
            }
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .status(UserStatus.PENDING_ACTIVATION)
                .deleted(false)
                .build();
        User saved = userRepository.save(user);

        // 3. Sinh token kích hoạt 48h lưu vào Redis
        String activationToken = UUID.randomUUID().toString();
        redisTokenService.saveActivationToken(activationToken, saved.getId(), ACTIVATION_TOKEN_TTL);

        // 4. Bắn email kích hoạt bất đồng bộ
        mailService.sendActivationEmail(saved.getEmail(), activationToken, ACTIVATION_TOKEN_TTL);

        log.info("Đã tạo tài khoản thành công cho user id={}", saved.getId());
        return userMapper.toResponse(saved);
    }

    /**
     * Lấy danh sách tài khoản phân trang có lọc theo từ khóa và trạng thái
     */
    public PageData<UserResponse> getUsers(UserFilterRequest filter) {
        log.debug("Lấy danh sách người dùng với filter: keyword={}, status={}",
                filter.getKeyword(), filter.getStatus());

        Pageable pageable = filter.toPageable("createdAt", UserFilterRequest.ALLOWED_SORT_FIELDS);
        Specification<User> spec = UserSpecification.build(filter);

        Page<User> page = userRepository.findAll(spec, pageable);
        return PageData.of(page, userMapper::toResponse);
    }

    /**
     * Gửi lại mã kích hoạt cho tài khoản PENDING_ACTIVATION
     */
    public void resendActivation(UUID userId) {
        User user = findUserOrThrow(userId);

        if (user.getStatus() != UserStatus.PENDING_ACTIVATION) {
            log.warn("Không thể gửi lại email kích hoạt cho user id={} vì trạng thái hiện tại là {}",
                    userId, user.getStatus());
            throw new AppException(AuthErrorCode.CANNOT_RESEND_ACTIVATION);
        }

        // Sinh token mới và cập nhật lại Redis 48h
        String newActivationToken = UUID.randomUUID().toString();
        redisTokenService.saveActivationToken(newActivationToken, user.getId(), ACTIVATION_TOKEN_TTL);

        // Bắn email
        mailService.sendActivationEmail(user.getEmail(), newActivationToken, ACTIVATION_TOKEN_TTL);
        log.info("Đã gửi lại email kích hoạt cho user id={}", userId);
    }

    /**
     * Khóa tài khoản và ngắt phiên làm việc
     */
    @Transactional
    public void lockUser(UUID userId) {
        log.warn("Khóa tài khoản user id={}", userId);
        User user = findUserOrThrow(userId);

        user.setStatus(UserStatus.LOCKED);
        userRepository.save(user);

        // Thu hồi toàn bộ Refresh Token trên Redis để ngắt session
        redisTokenService.deleteRefreshToken(userId);
        log.info("Đã khóa tài khoản và hủy phiên đăng nhập của user id={}", userId);
    }

    /**
     * Mở khóa tài khoản người dùng
     */
    @Transactional
    public void unlockUser(UUID userId) {
        log.info("Mở khóa tài khoản user id={}", userId);
        User user = findUserOrThrow(userId);

        // Chỉ mở khóa nếu tài khoản đã có mật khẩu (tức ACTIVE), nếu chưa từng kích hoạt thì về PENDING_ACTIVATION
        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
            user.setStatus(UserStatus.ACTIVE);
        } else {
            user.setStatus(UserStatus.PENDING_ACTIVATION);
        }
        userRepository.save(user);
        log.info("Đã mở khóa tài khoản user id={}", userId);
    }

    /**
     * Tạo hàng loạt tài khoản người dùng từ file Excel / danh sách
     */
    @Transactional
    public BulkCreateUserResponse bulkCreateUsers(BulkCreateUserRequest request) {
        List<BulkCreateUserItem> items = request.getUsers();
        log.info("Xử lý tạo hàng loạt tài khoản người dùng, tổng số yêu cầu: {}", items.size());

        List<UserResponse> createdUsers = new ArrayList<>();
        List<BulkUserErrorItem> errors = new ArrayList<>();
        Set<String> processedEmails = new HashSet<>();

        for (BulkCreateUserItem item : items) {
            if (item.getEmail() == null || item.getEmail().trim().isBlank()) {
                errors.add(BulkUserErrorItem.builder()
                        .email(item.getEmail())
                        .username(item.getUsername())
                        .reason("Email không được để trống")
                        .build());
                continue;
            }

            String email = item.getEmail().trim().toLowerCase();

            // Kiểm tra trùng lặp trong chính danh sách gửi lên
            if (!processedEmails.add(email)) {
                errors.add(BulkUserErrorItem.builder()
                        .email(email)
                        .username(item.getUsername())
                        .reason("Email bị trùng lặp trong danh sách gửi lên")
                        .build());
                continue;
            }

            // Kiểm tra trùng lặp email với CSDL
            if (userRepository.existsByEmailAndDeletedFalse(email)) {
                errors.add(BulkUserErrorItem.builder()
                        .email(email)
                        .username(item.getUsername())
                        .reason("Email đã tồn tại trong hệ thống")
                        .build());
                continue;
            }

            // Xác định username
            String username;
            if (item.getUsername() != null && !item.getUsername().trim().isBlank()) {
                username = item.getUsername().trim().toLowerCase();
                if (userRepository.existsByUsernameAndDeletedFalse(username)) {
                    username = username + "_" + (System.currentTimeMillis() % 10000);
                }
            } else {
                username = email.split("@")[0].toLowerCase();
                if (userRepository.existsByUsernameAndDeletedFalse(username)) {
                    username = username + "_" + (System.currentTimeMillis() % 10000);
                }
            }

            try {
                User user = User.builder()
                        .username(username)
                        .email(email)
                        .status(UserStatus.PENDING_ACTIVATION)
                        .deleted(false)
                        .build();
                User saved = userRepository.save(user);

                // Sinh token kích hoạt 48h lưu vào Redis
                String activationToken = UUID.randomUUID().toString();
                redisTokenService.saveActivationToken(activationToken, saved.getId(), ACTIVATION_TOKEN_TTL);

                // Gửi email kích hoạt bất đồng bộ
                mailService.sendActivationEmail(saved.getEmail(), activationToken, ACTIVATION_TOKEN_TTL);

                createdUsers.add(userMapper.toResponse(saved));
            } catch (Exception e) {
                log.error("Lỗi khi tạo tài khoản cho email {}: {}", email, e.getMessage());
                errors.add(BulkUserErrorItem.builder()
                        .email(email)
                        .username(username)
                        .reason("Lỗi hệ thống khi lưu tài khoản")
                        .build());
            }
        }

        log.info("Hoàn tất tạo hàng loạt: thành công={}, bỏ qua={}", createdUsers.size(), errors.size());

        return BulkCreateUserResponse.builder()
                .totalRequested(items.size())
                .totalCreated(createdUsers.size())
                .totalSkipped(errors.size())
                .createdUsers(createdUsers)
                .errors(errors)
                .build();
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));
    }
}
