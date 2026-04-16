package org.example.quizizz.common.cache.domain.auth;

import lombok.RequiredArgsConstructor;
import org.example.quizizz.common.cache.redis.RedisCacheExecutor;
import org.example.quizizz.common.constants.RedisKeyPrefix;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisTokenBlacklistCache implements TokenBlacklistCache {

    private static final String OP_BLACKLIST = "TOKEN_BLACKLIST_SAVE";
    private static final String OP_EXISTS = "TOKEN_BLACKLIST_EXISTS";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheExecutor cacheExecutor;

    @Override
    public void blacklist(String token, long refreshTokenExpiration) {
        if (token == null || token.isBlank()) {
            return;
        }

        long now = System.currentTimeMillis();
        long remainingTime = refreshTokenExpiration - now;
        if (remainingTime <= 0) {
            return;
        }

        String key = RedisKeyPrefix.TOKEN_BLACKLIST.format(token);
        cacheExecutor.executeWrite(OP_BLACKLIST, key,
                () -> redisTemplate.opsForValue().set(key, "blacklisted", remainingTime, TimeUnit.MILLISECONDS));
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String key = RedisKeyPrefix.TOKEN_BLACKLIST.format(token);
        return cacheExecutor.executeRead(OP_EXISTS, key,
                () -> Boolean.TRUE.equals(redisTemplate.hasKey(key)),
                false);
    }
}
