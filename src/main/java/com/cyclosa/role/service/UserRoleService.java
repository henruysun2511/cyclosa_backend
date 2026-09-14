package com.cyclosa.role.service;

import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.role.dto.request.AssignUserRolesRequest;
import com.cyclosa.role.dto.response.EffectivePermissionResponse;
import com.cyclosa.role.entity.Role;
import com.cyclosa.role.entity.RolePermission;
import com.cyclosa.role.entity.UserRoleMapping;
import com.cyclosa.role.repository.RolePermissionRepository;
import com.cyclosa.role.repository.RoleRepository;
import com.cyclosa.role.repository.UserRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleMappingRepository userRoleMappingRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserService userService; // Only calling UserService from auth module!

    @Transactional
    public void assignRolesToUser(UUID userId, AssignUserRolesRequest req) {
        User user = userService.findById(userId);

        userRoleMappingRepository.deleteByUserId(userId);
        user.getUserRoles().clear();

        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            List<UUID> roleIds = req.getRoles().stream()
                    .map(AssignUserRolesRequest.UserRoleItem::getRoleId)
                    .toList();

            Map<UUID, Role> roleMap = roleRepository.findAllById(roleIds)
                    .stream()
                    .collect(Collectors.toMap(Role::getId, r -> r));

            List<UserRoleMapping> toSave = new ArrayList<>();
            for (AssignUserRolesRequest.UserRoleItem item : req.getRoles()) {
                Role role = roleMap.get(item.getRoleId());
                if (role == null) {
                    throw AppException.roleNotFound(item.getRoleId());
                }

                UserRoleMapping urm = UserRoleMapping.builder()
                        .user(user)
                        .role(role)
                        .companyId(item.getCompanyId())
                        .build();
                toSave.add(urm);
            }

            userRoleMappingRepository.saveAll(toSave);
            user.getUserRoles().addAll(toSave);
        }
    }

    @Transactional
    public void assignDefaultRoleToUser(UUID userId, String roleCode, UUID companyId) {
        User user = userService.findById(userId);

        Role role = (companyId != null)
                ? roleRepository.findByCodeAndCompanyId(roleCode, companyId)
                        .orElseGet(() -> roleRepository.findByCodeAndCompanyIdIsNull(roleCode).orElse(null))
                : roleRepository.findByCodeAndCompanyIdIsNull(roleCode)
                        .orElseGet(() -> roleRepository.findByCode(roleCode).orElse(null));

        if (role != null) {
            UserRoleMapping mapping = UserRoleMapping.builder()
                    .user(user)
                    .role(role)
                    .companyId(companyId)
                    .build();
            userRoleMappingRepository.save(mapping);
            user.getUserRoles().add(mapping);
        }
    }

    @Transactional(readOnly = true)
    public List<EffectivePermissionResponse> getEffectivePermissions(UUID userId) {
        List<UserRoleMapping> mappings = userRoleMappingRepository.findByUserIdWithRole(userId);
        if (mappings.isEmpty()) {
            return List.of();
        }

        List<UUID> roleIds = mappings.stream().map(m -> m.getRole().getId()).toList();
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIdInWithPermission(roleIds);

        Map<String, RolePermission> effectiveMap = new HashMap<>();

        for (RolePermission rp : rolePermissions) {
            String code = rp.getPermission().getCode();
            RolePermission existing = effectiveMap.get(code);

            if (existing == null) {
                effectiveMap.put(code, rp);
            } else {
                if (rp.getDataScope().getLevel() > existing.getDataScope().getLevel()) {
                    effectiveMap.put(code, rp);
                }
            }
        }

        return effectiveMap.values().stream()
                .map(rp -> EffectivePermissionResponse.builder()
                        .permissionId(rp.getPermission().getId())
                        .permissionCode(rp.getPermission().getCode())
                        .module(rp.getPermission().getModule())
                        .action(rp.getPermission().getAction())
                        .description(rp.getPermission().getDescription())
                        .dataScope(rp.getDataScope())
                        .build())
                .sorted(Comparator.comparing(EffectivePermissionResponse::getModule)
                        .thenComparing(EffectivePermissionResponse::getPermissionCode))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getUserRoleCodes(UUID userId) {
        return userRoleMappingRepository.findByUserIdWithRole(userId).stream()
                .map(urm -> urm.getRole().getCode())
                .distinct()
                .toList();
    }
}
