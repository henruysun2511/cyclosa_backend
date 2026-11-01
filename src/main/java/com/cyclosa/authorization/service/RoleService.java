package com.cyclosa.authorization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.authorization.dto.request.AssignRolePermissionsRequest;
import com.cyclosa.authorization.dto.request.CreateRoleRequest;
import com.cyclosa.authorization.dto.request.RoleDocumentPermissionRequest;
import com.cyclosa.authorization.dto.request.RoleFilterRequest;
import com.cyclosa.authorization.dto.request.UpdateRoleRequest;
import com.cyclosa.authorization.dto.response.RolePermissionResponse;
import com.cyclosa.authorization.dto.response.RoleResponse;
import com.cyclosa.authorization.entity.Permission;
import com.cyclosa.authorization.entity.Role;
import com.cyclosa.authorization.entity.RoleDocumentTypePermission;
import com.cyclosa.authorization.entity.RolePermission;
import com.cyclosa.authorization.exception.RoleErrorCode;
import com.cyclosa.authorization.mapper.RoleMapper;
import com.cyclosa.authorization.repository.PermissionRepository;
import com.cyclosa.authorization.repository.RoleDocumentTypePermissionRepository;
import com.cyclosa.authorization.repository.RolePermissionRepository;
import com.cyclosa.authorization.repository.RoleRepository;
import com.cyclosa.authorization.repository.UserRoleAssignmentRepository;
import com.cyclosa.authorization.repository.specification.RoleSpecification;
import com.cyclosa.authorization.security.RoleCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final RoleDocumentTypePermissionRepository roleDocumentTypePermissionRepository;
    private final RoleCacheService roleCacheService;
    private final RoleMapper roleMapper;

    /**
     * Lọc và phân trang danh sách vai trò
     */
    @Transactional(readOnly = true)
    public PageData<RoleResponse> getRoles(RoleFilterRequest request) {
        Pageable pageable = request.toPageable("createdAt", RoleFilterRequest.ALLOWED_SORT_FIELDS);
        Page<Role> page = roleRepository.findAll(RoleSpecification.filterBy(request), pageable);
        return PageData.of(page, roleMapper::toResponse);
    }

    /**
     * Tạo mới vai trò tùy chỉnh
     */
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String cleanCode = request.getCode().trim().toUpperCase();

        // Kiểm tra trùng mã
        if (request.getCompanyId() != null) {
            if (roleRepository.existsByCompanyIdAndCodeAndDeletedFalse(request.getCompanyId(), cleanCode)) {
                throw new AppException(RoleErrorCode.ROLE_CODE_EXISTS);
            }
        } else {
            if (roleRepository.existsByCodeAndCompanyIdIsNullAndDeletedFalse(cleanCode)) {
                throw new AppException(RoleErrorCode.ROLE_CODE_EXISTS);
            }
        }

        Role role = roleMapper.toEntity(request);
        role.setCode(cleanCode);
        role.setIsSystemRole(false);

        Role savedRole = roleRepository.save(role);
        log.info("Tạo mới vai trò thành công: code={}, id={}", savedRole.getCode(), savedRole.getId());
        return roleMapper.toResponse(savedRole);
    }

    /**
     * Cập nhật thông tin vai trò (tên hiển thị và mô tả)
     */
    @Transactional
    public RoleResponse updateRole(UUID roleId, UpdateRoleRequest request) {
        Role role = findRoleEntity(roleId);

        roleMapper.updateEntityFromDto(request, role);
        Role updated = roleRepository.save(role);
        log.info("Cập nhật thông tin vai trò id={}", roleId);
        return roleMapper.toResponse(updated);
    }

    /**
     * Xóa mềm vai trò
     */
    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = findRoleEntity(roleId);

        if (Boolean.TRUE.equals(role.getIsSystemRole())) {
            throw new AppException(RoleErrorCode.SYSTEM_ROLE_PROTECTED);
        }

        long userCount = userRoleAssignmentRepository.countByRoleId(roleId);
        if (userCount > 0) {
            throw new AppException(RoleErrorCode.ROLE_IN_USE);
        }

        roleRepository.delete(role);
        roleCacheService.evictRolePermissions(roleId);
        log.info("Xóa mềm vai trò id={}", roleId);
    }

    /**
     * Lấy danh sách quyền và phạm vi dữ liệu được gán cho vai trò
     */
    @Transactional(readOnly = true)
    public List<RolePermissionResponse> getRolePermissions(UUID roleId) {
        findRoleEntity(roleId); // Đảm bảo role tồn tại
        List<RolePermission> permissions = rolePermissionRepository.findAllWithPermissionByRoleId(roleId);
        return roleMapper.toRolePermissionResponseList(permissions);
    }

    /**
     * Gán toàn bộ ma trận quyền kèm data_scope cho vai trò
     */
    @Transactional
    public List<RolePermissionResponse> assignPermissions(UUID roleId, AssignRolePermissionsRequest request) {
        Role role = findRoleEntity(roleId);

        // 1. Xóa các quyền cũ của vai trò
        rolePermissionRepository.deleteByRoleId(roleId);

        if (request.getPermissions() == null || request.getPermissions().isEmpty()) {
            roleCacheService.evictRolePermissions(roleId);
            List<UUID> affectedUsers = userRoleAssignmentRepository.findUserIdsByRoleId(roleId);
            roleCacheService.evictUsersPermissions(affectedUsers);
            return Collections.emptyList();
        }

        // 2. Nạp danh sách Permission Entities
        Set<UUID> permIds = request.getPermissions().stream()
                .map(AssignRolePermissionsRequest.PermissionScopeItem::getPermissionId)
                .collect(Collectors.toSet());
        Map<UUID, Permission> permMap = permissionRepository.findByIdIn(permIds).stream()
                .collect(Collectors.toMap(Permission::getId, p -> p));

        // 3. Tạo mới danh sách RolePermission
        List<RolePermission> newRolePerms = new ArrayList<>();
        for (AssignRolePermissionsRequest.PermissionScopeItem item : request.getPermissions()) {
            Permission perm = permMap.get(item.getPermissionId());
            if (perm == null) {
                throw new AppException(RoleErrorCode.PERMISSION_NOT_FOUND);
            }
            RolePermission rp = RolePermission.builder()
                    .role(role)
                    .permission(perm)
                    .dataScope(item.getDataScope())
                    .build();
            newRolePerms.add(rp);
        }

        List<RolePermission> saved = rolePermissionRepository.saveAll(newRolePerms);

        // 4. Invalidate & Refresh Cache
        roleCacheService.evictRolePermissions(roleId);
        List<UUID> affectedUsers = userRoleAssignmentRepository.findUserIdsByRoleId(roleId);
        roleCacheService.evictUsersPermissions(affectedUsers);

        log.info("Cập nhật {} quyền cho vai trò id={}, ảnh hưởng {} người dùng", saved.size(), roleId, affectedUsers.size());
        return roleMapper.toRolePermissionResponseList(saved);
    }

    /**
     * Cấu hình phân quyền truy cập hồ sơ tài liệu số cho vai trò
     */
    @Transactional
    public void configureDocumentPermissions(UUID roleId, RoleDocumentPermissionRequest request) {
        Role role = findRoleEntity(roleId);
        roleDocumentTypePermissionRepository.deleteByRoleId(roleId);

        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            List<RoleDocumentTypePermission> list = request.getPermissions().stream()
                    .map(p -> RoleDocumentTypePermission.builder()
                            .role(role)
                            .documentTypeId(p.getDocumentTypeId())
                            .action(p.getAction())
                            .dataScope(p.getDataScope())
                            .build())
                    .toList();
            roleDocumentTypePermissionRepository.saveAll(list);
            log.info("Cấu hình {} quyền tài liệu số cho vai trò id={}", list.size(), roleId);
        }
    }

    public Role findRoleEntity(UUID roleId) {
        return roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new AppException(RoleErrorCode.ROLE_NOT_FOUND));
    }
}
