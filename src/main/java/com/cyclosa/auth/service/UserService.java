package com.cyclosa.auth.service;

import com.cyclosa.auth.dto.request.CreateUserRequest;
import com.cyclosa.auth.dto.request.UserFilter;
import com.cyclosa.auth.dto.response.UserInfo;
import com.cyclosa.auth.dto.response.UserResponse;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.event.UserCreatedEvent;
import com.cyclosa.auth.mapper.AuthMapper;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.auth.exception.AuthErrorCode;
import com.cyclosa.common.enums.UserStatus;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final Set<String> SORT_FIELDS = Set.of("fullName", "email", "createdAt", "lastLoginAt");

    @Transactional(readOnly = true)
    public PageData<UserResponse> getUsers(UserFilter req) {
        String kw = PageableUtils.normalizeKeyword(req.getKeyword());
        Pageable pageable = req.toPageable("createdAt", SORT_FIELDS);
        Page<User> result = userRepository.searchUsers(kw, req.getStatus(), pageable);
        return PageData.of(result, authMapper::toUserResponse);
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> AppException.userNotFound(id));
    }

    @Transactional(readOnly = true)
    public User findByIdWithRoles(UUID id) {
        return userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> AppException.userNotFound(id));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByIdOptional(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public User findByEmailWithRoles(String email) {
        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(AppException::invalidCredentials);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmployeeId(UUID employeeId) {
        return userRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public java.util.Map<UUID, com.cyclosa.common.dto.summary.UserSummary> getUserSummaries(java.util.Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Map.of();
        }
        return userRepository.findAllById(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        User::getId,
                        u -> com.cyclosa.common.dto.summary.UserSummary.builder()
                                .id(u.getId())
                                .username(u.getUsername())
                                .fullName(u.getFullName())
                                .email(u.getEmail())
                                .avatarUrl(u.getAvatarUrl())
                                .build()
                ));
    }

    @Transactional(readOnly = true)
    public UserInfo getUserInfo(UUID id) {
        User user = findByIdWithRoles(id);
        return authMapper.toUserInfo(user);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void updateLastLogin(UUID id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void updateUserStatus(UUID userId, UserStatus status) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(status);
            userRepository.save(user);
            log.info("Updated User id={} status to {}", userId, status);
        });
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest req) {
        String cleanEmail = req.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw AppException.userEmailExists(cleanEmail);
        }

        User user = User.builder()
                .employeeId(req.getEmployeeId())
                .email(cleanEmail)
                .username(cleanEmail)
                .fullName(req.getFullName().trim())
                .phone(req.getPhone() != null ? req.getPhone().trim() : null)
                .status(UserStatus.PENDING_ACTIVATION)
                .version(0)
                .build();

        user = userRepository.save(user);

        // Publish event để module role tự động gán vai trò theo mô hình Modular Monolith tách biệt
        List<String> roleCodes = (req.getRoleCodes() != null && !req.getRoleCodes().isEmpty())
                ? req.getRoleCodes()
                : List.of("EMPLOYEE");

        eventPublisher.publishEvent(UserCreatedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .roleCodes(roleCodes)
                .companyId(req.getCompanyId())
                .build());

        // Generate activation token
        String activationToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                AuthService.ACTIVATION_TOKEN_PREFIX + activationToken,
                user.getId().toString(),
                48, TimeUnit.HOURS
        );
        redisTemplate.opsForValue().set(
                AuthService.USER_ACTIVATION_PREFIX + user.getId(),
                activationToken,
                48, TimeUnit.HOURS
        );

        // Send activation email
        emailService.sendActivationEmail(user.getEmail(), user.getFullName(), activationToken);

        return authMapper.toUserResponse(user);
    }

    @Transactional
    public void resendActivation(UUID userId) {
        User user = findById(userId);
        if (user.getStatus() != UserStatus.PENDING_ACTIVATION) {
            throw AppException.validationFailed("Tài khoản này không ở trạng thái chờ kích hoạt");
        }

        // Delete old token if exists
        String oldToken = redisTemplate.opsForValue().get(AuthService.USER_ACTIVATION_PREFIX + userId);
        if (oldToken != null) {
            redisTemplate.delete(AuthService.ACTIVATION_TOKEN_PREFIX + oldToken);
        }

        // Generate new token
        String activationToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                AuthService.ACTIVATION_TOKEN_PREFIX + activationToken,
                user.getId().toString(),
                48, TimeUnit.HOURS
        );
        redisTemplate.opsForValue().set(
                AuthService.USER_ACTIVATION_PREFIX + user.getId(),
                activationToken,
                48, TimeUnit.HOURS
        );

        emailService.sendActivationEmail(user.getEmail(), user.getFullName(), activationToken);
    }

    @Transactional
    public UserResponse lockUser(UUID userId) {
        User user = findById(userId);
        user.setStatus(UserStatus.LOCKED);
        user = userRepository.save(user);

        // Invalidate active refresh token in Redis
        redisTemplate.delete(AuthService.REFRESH_TOKEN_PREFIX + userId);

        return authMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse unlockUser(UUID userId) {
        User user = findById(userId);
        if (user.getPasswordHash() == null) {
            user.setStatus(UserStatus.PENDING_ACTIVATION);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }
        user = userRepository.save(user);

        return authMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserDetail(UUID userId) {
        User user = findByIdWithRoles(userId);
        return authMapper.toUserResponse(user);
    }

    @Transactional
    public void linkEmployeeToUser(UUID userId, UUID employeeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.userNotFound(userId));
        if (user.getEmployeeId() != null && !user.getEmployeeId().equals(employeeId)) {
            throw new AppException(AuthErrorCode.USER_ALREADY_LINKED);
        }
        user.setEmployeeId(employeeId);
        userRepository.save(user);
        log.info("Linked Employee id={} to User id={}", employeeId, userId);
    }

    @Transactional
    public UUID createEmployeeUser(UUID employeeId, String email, String fullName, String employeeCode, UUID companyId) {
        String cleanEmail = email.trim().toLowerCase();
        if (userRepository.findByEmail(cleanEmail).isPresent()) {
            return null;
        }
        User newUser = User.builder()
                .email(cleanEmail)
                .username(cleanEmail.split("@")[0] + "_" + employeeCode.toLowerCase().replace("-", "_"))
                .fullName(fullName)
                .passwordHash(passwordEncoder.encode("Cyclosa@123"))
                .employeeId(employeeId)
                .status(UserStatus.ACTIVE)
                .version(0)
                .build();
        newUser = userRepository.save(newUser);
        log.info("Auto-created User id={} for Employee code={}", newUser.getId(), employeeCode);
        return newUser.getId();
    }
}
