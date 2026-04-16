package org.example.quizizz.common.cache.domain.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.quizizz.common.cache.config.RedisCacheProperties;
import org.example.quizizz.common.cache.redis.RedisCacheExecutor;
import org.example.quizizz.common.constants.PermissionCode;
import org.example.quizizz.common.constants.RedisKeyPrefix;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUserPermissionCache implements UserPermissionCache {

    private static final String OP_SAVE = "USER_PERMISSION_SAVE";
    private static final String OP_GET = "USER_PERMISSION_GET";
    private static final String OP_EVICT = "USER_PERMISSION_EVICT";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheExecutor cacheExecutor;
    private final RedisCacheProperties cacheProperties;

    @Override
    public void save(Long userId, Set<PermissionCode> permissions) {
        if (userId == null) {
            return;
        }

        String key = RedisKeyPrefix.USER_PERMISSIONS.format(userId);
        cacheExecutor.executeWrite(OP_SAVE, key, () -> {
            redisTemplate.delete(key);
            if (permissions == null || permissions.isEmpty()) {
                return;
            }

            Set<String> permissionCodes = permissions.stream()
                    .filter(Objects::nonNull)
                    .map(PermissionCode::getCode)
                    .collect(Collectors.toSet());

            if (permissionCodes.isEmpty()) {
                return;
            }

            redisTemplate.opsForSet().add(key, permissionCodes.toArray());
            redisTemplate.expire(key, cacheProperties.getUserPermissionsTtlHours(), TimeUnit.HOURS);
        });
    }

    @Override
    public Set<PermissionCode> get(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        String key = RedisKeyPrefix.USER_PERMISSIONS.format(userId);
        return cacheExecutor.executeRead(OP_GET, key, () -> {
            Set<Object> data = redisTemplate.opsForSet().members(key);
            if (data == null || data.isEmpty()) {
                return Collections.emptySet();
            }

            return data.stream()
                    .map(Object::toString)
                    .map(code -> PermissionCode.fromCode(code).orElseGet(() -> {
                        log.warn("Unknown permission code in cache. userId={}, code={}", userId, code);
                        return null;
                    }))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }, Collections.emptySet());
    }

    @Override
    public void evict(Long userId) {
        if (userId == null) {
            return;
        }
        String key = RedisKeyPrefix.USER_PERMISSIONS.format(userId);
        cacheExecutor.executeWrite(OP_EVICT, key, () -> redisTemplate.delete(key));
    }
}
