package com.cyclosa.role.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.permission.entity.Permission;
import com.cyclosa.permission.service.PermissionService;
import com.cyclosa.role.dto.request.AssignRolePermissionsRequest;
import com.cyclosa.role.dto.request.RoleRequest;
import com.cyclosa.role.dto.response.RoleDetailResponse;
import com.cyclosa.role.dto.response.RoleResponse;
import com.cyclosa.role.entity.Role;
import com.cyclosa.role.entity.RolePermission;
import com.cyclosa.role.mapper.RoleMapper;
import com.cyclosa.role.repository.RolePermissionRepository;
import com.cyclosa.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionService permissionService; // Only calling PermissionService from permission module!
    private final RoleMapper roleMapper;

    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles(UUID companyId) {
        List<Role> roles = (companyId != null)
                ? roleRepository.findByCompanyIdOrCompanyIdIsNull(companyId)
                : roleRepository.findByCompanyIdIsNull();
        return roleMapper.toResponseList(roles);
    }

    @Transactional(readOnly = true)
    public RoleDetailResponse getRoleById(UUID id) {
        Role role = findRoleById(id);
        List<RolePermission> permissions = rolePermissionRepository.findByRoleIdWithPermission(id);
        role.setRolePermissions(permissions);
        return roleMapper.toDetailResponse(role);
    }

    @Transactional
    public RoleResponse createRole(RoleRequest req) {
        boolean exists = (req.getCompanyId() != null)
                ? roleRepository.existsByCodeAndCompanyId(req.getCode(), req.getCompanyId())
                : roleRepository.existsByCodeAndCompanyIdIsNull(req.getCode());

        if (exists) {
            throw AppException.conflict("Mã vai trò '" + req.getCode() + "' đã tồn tại trong công ty");
        }

        Role role = roleMapper.toEntity(req);
        role.setSystemRole(false);
        Role saved = roleRepository.save(role);
        return roleMapper.toResponse(saved);
    }

    @Transactional
    public RoleResponse updateRole(UUID id, RoleRequest req) {
        Role role = findRoleById(id);

        if (role.isSystemRole()) {
            throw AppException.forbidden("Không được phép chỉnh sửa vai trò hệ thống");
        }

        role.setName(req.getName());
        role.setDescription(req.getDescription());
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(UUID id) {
        Role role = findRoleById(id);
        if (role.isSystemRole()) {
            throw AppException.forbidden("Không thể xóa vai trò hệ thống");
        }
        roleRepository.delete(role);
    }

    @Transactional
    public RoleDetailResponse assignPermissions(UUID roleId, AssignRolePermissionsRequest req) {
        Role role = findRoleById(roleId);

        rolePermissionRepository.deleteByRoleId(roleId);
        role.getRolePermissions().clear();

        if (req.getPermissions() != null && !req.getPermissions().isEmpty()) {
            List<UUID> permIds = req.getPermissions().stream()
                    .map(AssignRolePermissionsRequest.PermissionAssignment::getPermissionId)
                    .toList();

            Map<UUID, Permission> permMap = permissionService.findAllById(permIds)
                    .stream()
                    .collect(Collectors.toMap(Permission::getId, p -> p));

            List<RolePermission> newPermissions = new ArrayList<>();
            for (AssignRolePermissionsRequest.PermissionAssignment item : req.getPermissions()) {
                Permission perm = permMap.get(item.getPermissionId());
                if (perm == null) {
                    throw AppException.notFound("Permission not found with id: " + item.getPermissionId());
                }

                RolePermission rp = RolePermission.builder()
                        .role(role)
                        .permission(perm)
                        .dataScope(item.getDataScope())
                        .build();
                newPermissions.add(rp);
            }

            rolePermissionRepository.saveAll(newPermissions);
            role.getRolePermissions().addAll(newPermissions);
        }

        return roleMapper.toDetailResponse(role);
    }

    public Role findRoleById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> AppException.roleNotFound(id));
    }
}
