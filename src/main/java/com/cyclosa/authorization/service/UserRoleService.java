package com.cyclosa.authorization.service;

import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.authorization.dto.request.AssignUserRoleRequest;
import com.cyclosa.authorization.dto.response.EffectivePermissionResponse;
import com.cyclosa.authorization.dto.response.UserRoleAssignmentResponse;
import com.cyclosa.authorization.entity.Role;
import com.cyclosa.authorization.entity.RolePermission;
import com.cyclosa.authorization.entity.UserRoleAssignment;
import com.cyclosa.authorization.exception.RoleErrorCode;
import com.cyclosa.authorization.repository.RolePermissionRepository;
import com.cyclosa.authorization.repository.RoleRepository;
import com.cyclosa.authorization.repository.UserRoleAssignmentRepository;
import com.cyclosa.authorization.security.RoleCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleCacheService roleCacheService;

    /**
     * Lấy danh sách các vai trò được gán cho người dùng
     */
    @Transactional(readOnly = true)
    public List<UserRoleAssignmentResponse> getUserRoles(UUID userId) {
        List<UserRoleAssignment> list = userRoleAssignmentRepository.findAllWithRoleByUserId(userId);
        return list.stream().map(this::toAssignmentResponse).toList();
    }

    /**
     * Gán vai trò cho người dùng tại một công ty cụ thể
     */
    @Transactional
    public UserRoleAssignmentResponse assignRoleToUser(UUID userId, AssignUserRoleRequest request) {
        // 1. Kiểm tra User tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy người dùng"));

        // 2. Kiểm tra Role tồn tại
        Role role = roleRepository.findByIdAndDeletedFalse(request.getRoleId())
                .orElseThrow(() -> new AppException(RoleErrorCode.ROLE_NOT_FOUND));

        // 3. Kiểm tra trùng lặp
        if (userRoleAssignmentRepository.existsByUserIdAndRoleIdAndCompanyId(userId, request.getRoleId(), request.getCompanyId())) {
            throw new AppException(RoleErrorCode.USER_ROLE_ASSIGNMENT_EXISTS);
        }

        UserRoleAssignment assignment = UserRoleAssignment.builder()
                .userId(user.getId())
                .role(role)
                .companyId(request.getCompanyId())
                .build();

        UserRoleAssignment saved = userRoleAssignmentRepository.save(assignment);

        // 4. Invalidate Cache
        roleCacheService.evictUserPermissions(userId, request.getCompanyId());

        log.info("Gán vai trò code={} cho userId={} tại companyId={}", role.getCode(), userId, request.getCompanyId());
        return toAssignmentResponse(saved);
    }

    /**
     * Thu hồi vai trò đã gán cho người dùng
     */
    @Transactional
    public void revokeRoleAssignment(UUID assignmentId) {
        UserRoleAssignment assignment = userRoleAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(RoleErrorCode.ASSIGNMENT_NOT_FOUND));

        UUID userId = assignment.getUserId();
        UUID companyId = assignment.getCompanyId();

        userRoleAssignmentRepository.delete(assignment);

        // Invalidate Cache
        roleCacheService.evictUserPermissions(userId, companyId);
        log.info("Thu hồi bản ghi gán vai trò id={}, userId={}, companyId={}", assignmentId, userId, companyId);
    }

    /**
     * Tính toán hoặc lấy từ Cache danh sách quyền hiệu lực của người dùng tại công ty
     */
    @Transactional(readOnly = true)
    public Map<String, DataScope> calculateEffectivePermissions(UUID userId, UUID companyId) {
        // 1. Kiểm tra Cache Redis
        Optional<Map<String, DataScope>> cached = roleCacheService.getEffectivePermissions(userId, companyId);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 2. Cache Miss: Tính toán từ CSDL
        List<UserRoleAssignment> assignments = userRoleAssignmentRepository
                .findAllWithRoleByUserIdAndCompanyId(userId, companyId);

        if (assignments.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UUID> roleIds = assignments.stream().map(a -> a.getRole().getId()).toList();
        List<RolePermission> rolePerms = rolePermissionRepository.findAllWithPermissionByRoleIdIn(roleIds);

        // 3. Giải quyết xung đột quyền: Lấy DataScope rộng nhất (max DataScope)
        Map<String, DataScope> effectiveMap = new HashMap<>();
        for (RolePermission rp : rolePerms) {
            String code = rp.getPermission().getCode();
            DataScope currentScope = rp.getDataScope();
            effectiveMap.merge(code, currentScope, DataScope::max);
        }

        // 4. Lưu lại vào Redis Cache
        roleCacheService.cacheEffectivePermissions(userId, companyId, effectiveMap);

        // Cache luôn danh sách tên role
        Set<String> roleCodes = assignments.stream().map(a -> a.getRole().getCode()).collect(Collectors.toSet());
        roleCacheService.cacheUserRoles(userId, companyId, roleCodes);

        return effectiveMap;
    }

    /**
     * Lấy danh sách DTO quyền hiệu lực để trả về Client
     */
    @Transactional(readOnly = true)
    public List<EffectivePermissionResponse> getEffectivePermissionsResponse(UUID userId, UUID companyId) {
        Map<String, DataScope> perms = calculateEffectivePermissions(userId, companyId);
        return perms.entrySet().stream()
                .map(e -> EffectivePermissionResponse.builder()
                        .permissionCode(e.getKey())
                        .highestDataScope(e.getValue())
                        .build())
                .sorted(Comparator.comparing(EffectivePermissionResponse::getPermissionCode))
                .toList();
    }

    private UserRoleAssignmentResponse toAssignmentResponse(UserRoleAssignment a) {
        return UserRoleAssignmentResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .roleId(a.getRole().getId())
                .roleCode(a.getRole().getCode())
                .roleName(a.getRole().getName())
                .companyId(a.getCompanyId())
                .createdAt(a.getCreatedAt())
                .build();
    }
}

