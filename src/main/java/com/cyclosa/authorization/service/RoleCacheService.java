package com.cyclosa.authorization.security;

import com.cyclosa.common.enums.DataScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleCacheService {

    private static final String ROLE_PERMS_PREFIX = "auth:role:permissions:";
    private static final String USER_ROLES_PREFIX = "auth:user:roles:";
    private static final String USER_EFFECTIVE_PERMS_PREFIX = "auth:user:effective_perms:";

    private static final Duration ROLE_PERMS_TTL = Duration.ofHours(24);
    private static final Duration USER_PERMS_TTL = Duration.ofHours(12);

    private final StringRedisTemplate redisTemplate;

    /**
     * Lấy danh sách quyền và phạm vi dữ liệu đã được gán cho một vai trò từ cache
     */
    public Optional<Map<String, DataScope>> getRolePermissions(UUID roleId) {
        String key = ROLE_PERMS_PREFIX + roleId;
        Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(key);
        if (rawEntries == null || rawEntries.isEmpty()) {
            return Optional.empty();
        }

        Map<String, DataScope> result = new HashMap<>();
        rawEntries.forEach((k, v) -> {
            try {
                result.put(k.toString(), DataScope.valueOf(v.toString()));
            } catch (Exception e) {
                log.warn("Không thể parse DataScope từ cache cho roleId={}: {}", roleId, v);
            }
        });
        return Optional.of(result);
    }

    /**
     * Lưu danh sách quyền của một vai trò vào cache
     */
    public void cacheRolePermissions(UUID roleId, Map<String, DataScope> perms) {
        String key = ROLE_PERMS_PREFIX + roleId;
        if (perms == null || perms.isEmpty()) {
            redisTemplate.delete(key);
            return;
        }

        Map<String, String> stringMap = perms.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().name()));

        redisTemplate.opsForHash().putAll(key, stringMap);
        redisTemplate.expire(key, ROLE_PERMS_TTL);
        log.debug("Đã cache {} quyền cho roleId={}", stringMap.size(), roleId);
    }

    /**
     * Xóa cache danh sách quyền của vai trò
     */
    public void evictRolePermissions(UUID roleId) {
        String key = ROLE_PERMS_PREFIX + roleId;
        redisTemplate.delete(key);
        log.debug("Đã xóa cache quyền của roleId={}", roleId);
    }

    /**
     * Lấy các vai trò của người dùng tại công ty
     */
    public Optional<Set<String>> getUserRoles(UUID userId, UUID companyId) {
        String key = userRolesKey(userId, companyId);
        Set<String> roles = redisTemplate.opsForSet().members(key);
        if (roles == null || roles.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(roles);
    }

    /**
     * Lưu các vai trò của người dùng tại công ty vào Redis Set
     */
    public void cacheUserRoles(UUID userId, UUID companyId, Set<String> roles) {
        String key = userRolesKey(userId, companyId);
        redisTemplate.delete(key);
        if (roles != null && !roles.isEmpty()) {
            redisTemplate.opsForSet().add(key, roles.toArray(new String[0]));
            redisTemplate.expire(key, USER_PERMS_TTL);
        }
    }

    /**
     * Lấy ma trận quyền hiệu lực của người dùng tại công ty (sau khi đã tính max DataScope)
     */
    public Optional<Map<String, DataScope>> getEffectivePermissions(UUID userId, UUID companyId) {
        String key = userEffectivePermsKey(userId, companyId);
        Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(key);
        if (rawEntries == null || rawEntries.isEmpty()) {
            return Optional.empty();
        }

        Map<String, DataScope> result = new HashMap<>();
        rawEntries.forEach((k, v) -> {
            try {
                result.put(k.toString(), DataScope.valueOf(v.toString()));
            } catch (Exception e) {
                log.warn("Không thể parse DataScope từ cache cho userId={}: {}", userId, v);
            }
        });
        return Optional.of(result);
    }

    /**
     * Lưu ma trận quyền hiệu lực của người dùng tại công ty
     */
    public void cacheEffectivePermissions(UUID userId, UUID companyId, Map<String, DataScope> effectivePerms) {
        String key = userEffectivePermsKey(userId, companyId);
        if (effectivePerms == null || effectivePerms.isEmpty()) {
            redisTemplate.delete(key);
            return;
        }

        Map<String, String> stringMap = effectivePerms.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().name()));

        redisTemplate.opsForHash().putAll(key, stringMap);
        redisTemplate.expire(key, USER_PERMS_TTL);
        log.debug("Đã cache {} quyền hiệu lực cho userId={}, companyId={}", stringMap.size(), userId, companyId);
    }

    /**
     * Xóa cache vai trò và quyền hiệu lực của người dùng tại một công ty (hoặc toàn bộ)
     */
    public void evictUserPermissions(UUID userId, UUID companyId) {
        if (companyId != null) {
            redisTemplate.delete(userRolesKey(userId, companyId));
            redisTemplate.delete(userEffectivePermsKey(userId, companyId));
            log.debug("Đã xóa cache quyền của userId={}, companyId={}", userId, companyId);
        } else {
            // Xóa mọi key liên quan đến userId này
            Set<String> roleKeys = redisTemplate.keys(USER_ROLES_PREFIX + userId + ":*");
            if (roleKeys != null && !roleKeys.isEmpty()) {
                redisTemplate.delete(roleKeys);
            }
            Set<String> permKeys = redisTemplate.keys(USER_EFFECTIVE_PERMS_PREFIX + userId + ":*");
            if (permKeys != null && !permKeys.isEmpty()) {
                redisTemplate.delete(permKeys);
            }
            log.debug("Đã xóa toàn bộ cache quyền của userId={}", userId);
        }
    }

    /**
     * Xóa cache quyền của nhiều người dùng bị ảnh hưởng khi vai trò bị chỉnh sửa
     */
    public void evictUsersPermissions(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        for (UUID uid : userIds) {
            evictUserPermissions(uid, null);
        }
    }

    private String userRolesKey(UUID userId, UUID companyId) {
        return USER_ROLES_PREFIX + userId + ":" + (companyId != null ? companyId : "global");
    }

    private String userEffectivePermsKey(UUID userId, UUID companyId) {
        return USER_EFFECTIVE_PERMS_PREFIX + userId + ":" + (companyId != null ? companyId : "global");
    }
}

