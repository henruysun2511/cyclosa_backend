package com.cyclosa.role.service;

import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.event.UserCreatedEvent;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleMappingRepository userRoleMappingRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserService userService; // Only calling UserService from auth module!
    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public static final String PERMISSION_CACHE_PREFIX = "user:permissions:";
    private static final long PERMISSION_CACHE_TTL_MINUTES = 30;

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

        evictUserPermissionCache(userId);
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
            evictUserPermissionCache(userId);
        }
    }

    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("[UserRoleService] Lắng nghe UserCreatedEvent cho userId: {}", event.getUserId());
        List<String> roleCodes = (event.getRoleCodes() != null && !event.getRoleCodes().isEmpty())
                ? event.getRoleCodes()
                : List.of("EMPLOYEE");

        for (String roleCode : roleCodes) {
            assignDefaultRoleToUser(event.getUserId(), roleCode, event.getCompanyId());
        }
    }

    @Transactional(readOnly = true)
    public List<EffectivePermissionResponse> getEffectivePermissions(UUID userId) {
        String cacheKey = PERMISSION_CACHE_PREFIX + userId;

        if (redisTemplate != null) {
            try {
                String cachedJson = redisTemplate.opsForValue().get(cacheKey);
                if (cachedJson != null) {
                    return objectMapper.readValue(cachedJson, new TypeReference<List<EffectivePermissionResponse>>() {});
                }
            } catch (Exception e) {
                log.warn("[UserRoleService] Đọc cache phân quyền thất bại, truy vấn trực tiếp DB: {}", e.getMessage());
            }
        }

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

        List<EffectivePermissionResponse> result = effectiveMap.values().stream()
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

        if (redisTemplate != null) {
            try {
                String json = objectMapper.writeValueAsString(result);
                redisTemplate.opsForValue().set(cacheKey, json, PERMISSION_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("[UserRoleService] Ghi cache phân quyền thất bại: {}", e.getMessage());
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<String> getUserRoleCodes(UUID userId) {
        return userRoleMappingRepository.findByUserIdWithRole(userId).stream()
                .map(urm -> urm.getRole().getCode())
                .distinct()
                .toList();
    }

    public long countUsersWithRole(UUID roleId) {
        return userRoleMappingRepository.countByRoleId(roleId);
    }

    public void evictUserPermissionCache(UUID userId) {
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(PERMISSION_CACHE_PREFIX + userId);
            } catch (Exception e) {
                log.warn("[UserRoleService] Xóa cache user {} thất bại: {}", userId, e.getMessage());
            }
        }
    }

    public void evictAllPermissionCache() {
        if (redisTemplate != null) {
            try {
                Set<String> keys = redisTemplate.keys(PERMISSION_CACHE_PREFIX + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                log.warn("[UserRoleService] Xóa toàn bộ cache phân quyền thất bại: {}", e.getMessage());
            }
        }
    }
}
