package com.cyclosa.auth.service;

import com.cyclosa.role.service.UserRoleService;
import com.cyclosa.auth.dto.request.LoginRequest;
import com.cyclosa.auth.dto.request.RefreshTokenRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Value("${app.jwt.access-token-expiry:86400000}")
    private long accessTokenExpiry;

    private static final String BLACKLIST_PREFIX     = "blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

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
            throw AppException.invalidCredentials();
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw AppException.invalidCredentials();
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

        User user = userRepository.findById(userId)
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.userNotFound(userId));
        return authMapper.toUserInfo(user);
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
