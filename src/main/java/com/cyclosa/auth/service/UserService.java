package com.cyclosa.auth.service;

import com.cyclosa.auth.dto.response.UserInfo;
import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.mapper.AuthMapper;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthMapper authMapper;

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
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
    public UserInfo getUserInfo(UUID id) {
        User user = findById(id);
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
}
