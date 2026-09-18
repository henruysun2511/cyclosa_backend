package com.cyclosa.role.service;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.permission.entity.Permission;
import com.cyclosa.permission.service.PermissionService;
import com.cyclosa.role.dto.request.AssignRolePermissionsRequest;
import com.cyclosa.role.dto.request.RoleFilter;
import com.cyclosa.role.dto.request.RoleRequest;
import com.cyclosa.role.dto.response.RoleDetailResponse;
import com.cyclosa.role.dto.response.RoleResponse;
import com.cyclosa.role.entity.Role;
import com.cyclosa.role.entity.RolePermission;
import com.cyclosa.role.mapper.RoleMapper;
import com.cyclosa.role.repository.RolePermissionRepository;
import com.cyclosa.role.repository.RoleRepository;
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
    private final PermissionService permissionService;
    private final UserRoleService userRoleService;
    private final CompanyService companyService;
    private final RoleMapper roleMapper;

    /** Allowed sort fields for roles */
    private static final Set<String> SORT_FIELDS = Set.of("name", "code", "createdAt");

    @Transactional(readOnly = true)
    public PageData<RoleResponse> getRoles(RoleFilter req) {
        String kw = PageableUtils.normalizeKeyword(req.getKeyword());
        Pageable pageable = req.toPageable("name", SORT_FIELDS);
        Page<Role> result = roleRepository.search(kw, req.getCompanyId(), req.getIsSystemRole(), pageable);

        Set<UUID> companyIds = result.getContent().stream()
                .map(Role::getCompanyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, CompanySummary> companyMap = companyService.getCompanySummaries(companyIds);

        List<RoleResponse> items = result.getContent().stream().map(role -> {
            RoleResponse response = roleMapper.toResponse(role);
            if (role.getCompanyId() != null) {
                response.setCompany(companyMap.get(role.getCompanyId()));
            }
            return response;
        }).toList();

        return PageData.of(result, items);
    }

    @Transactional(readOnly = true)
    public RoleDetailResponse getRoleById(UUID id) {
        Role role = findRoleById(id);
        List<RolePermission> permissions = rolePermissionRepository.findByRoleIdWithPermission(id);
        role.setRolePermissions(permissions);

        RoleDetailResponse response = roleMapper.toDetailResponse(role);
        if (role.getCompanyId() != null) {
            response.setCompany(companyService.getCompanySummary(role.getCompanyId()));
        }
        return response;
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

        RoleResponse response = roleMapper.toResponse(saved);
        if (saved.getCompanyId() != null) {
            response.setCompany(companyService.getCompanySummary(saved.getCompanyId()));
        }
        return response;
    }

    @Transactional
    public RoleResponse updateRole(UUID id, RoleRequest req) {
        Role role = findRoleById(id);

        if (role.isSystemRole()) {
            throw AppException.forbidden("Không được phép chỉnh sửa vai trò hệ thống");
        }

        role.setName(req.getName());
        role.setDescription(req.getDescription());
        Role saved = roleRepository.save(role);

        RoleResponse response = roleMapper.toResponse(saved);
        if (saved.getCompanyId() != null) {
            response.setCompany(companyService.getCompanySummary(saved.getCompanyId()));
        }
        return response;
    }

    @Transactional
    public void deleteRole(UUID id) {
        Role role = findRoleById(id);
        if (role.isSystemRole()) {
            throw AppException.cannotDeleteSystemRole();
        }

        long inUseCount = userRoleService.countUsersWithRole(id);
        if (inUseCount > 0) {
            throw AppException.roleInUse(inUseCount);
        }

        roleRepository.delete(role);
        userRoleService.evictAllPermissionCache();
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

        userRoleService.evictAllPermissionCache();

        RoleDetailResponse response = roleMapper.toDetailResponse(role);
        if (role.getCompanyId() != null) {
            response.setCompany(companyService.getCompanySummary(role.getCompanyId()));
        }
        return response;
    }

    public Role findRoleById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> AppException.roleNotFound(id));
    }
}
