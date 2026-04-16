package org.example.quizizz.common.cache.domain.presence;

import lombok.RequiredArgsConstructor;
import org.example.quizizz.common.cache.config.RedisCacheProperties;
import org.example.quizizz.common.cache.redis.RedisCacheExecutor;
import org.example.quizizz.common.constants.RedisKeyPrefix;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisOnlineUserCache implements OnlineUserCache {

    private static final String OP_MARK_ONLINE = "ONLINE_USER_MARK_ONLINE";
    private static final String OP_MARK_OFFLINE = "ONLINE_USER_MARK_OFFLINE";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheExecutor cacheExecutor;
    private final RedisCacheProperties cacheProperties;

    @Override
    public void markOnline(Long userId) {
        if (userId == null) {
            return;
        }

        String key = RedisKeyPrefix.USERS_ONLINE.getPattern();
        cacheExecutor.executeWrite(OP_MARK_ONLINE, key, () -> {
            redisTemplate.opsForSet().add(key, userId.toString());
            redisTemplate.expire(key, cacheProperties.getOnlineUsersTtlHours(), TimeUnit.HOURS);
        });
    }

    @Override
    public void markOffline(Long userId) {
        if (userId == null) {
            return;
        }

        String key = RedisKeyPrefix.USERS_ONLINE.getPattern();
        cacheExecutor.executeWrite(OP_MARK_OFFLINE, key,
                () -> redisTemplate.opsForSet().remove(key, userId.toString()));
    }
}
