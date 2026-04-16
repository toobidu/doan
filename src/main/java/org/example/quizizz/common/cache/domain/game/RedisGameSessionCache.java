package org.example.quizizz.common.cache.domain.game;

import lombok.RequiredArgsConstructor;
import org.example.quizizz.common.cache.CacheLookupResult;
import org.example.quizizz.common.cache.config.RedisCacheProperties;
import org.example.quizizz.common.cache.redis.RedisCacheExecutor;
import org.example.quizizz.common.constants.GameStatus;
import org.example.quizizz.common.constants.RedisKeyPrefix;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisGameSessionCache implements GameSessionCache {

    private static final String OP_SAVE = "GAME_SESSION_SAVE";
    private static final String OP_GET = "GAME_SESSION_GET";
    private static final String OP_UPDATE = "GAME_SESSION_UPDATE";
    private static final String OP_EVICT = "GAME_SESSION_EVICT";
    private static final String NULL_MARKER_FIELD = "__null__";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheExecutor cacheExecutor;
    private final RedisCacheProperties cacheProperties;

    @Override
    public void save(String gameId, Map<String, Object> sessionData) {
        if (gameId == null || gameId.isBlank()) {
            return;
        }

        String key = RedisKeyPrefix.GAME_SESSION.format(gameId);
        cacheExecutor.executeWrite(OP_SAVE, key, () -> {
            redisTemplate.delete(key);

            if (sessionData == null) {
                redisTemplate.opsForHash().put(key, NULL_MARKER_FIELD, Boolean.TRUE);
                expire(key);
                return;
            }

            Map<String, Object> sanitizedData = new HashMap<>();
            sessionData.forEach((mapKey, value) -> {
                if (mapKey != null && !mapKey.isBlank() && value != null) {
                    sanitizedData.put(mapKey, value);
                }
            });

            if (sanitizedData.isEmpty()) {
                redisTemplate.opsForHash().put(key, NULL_MARKER_FIELD, Boolean.TRUE);
            } else {
                redisTemplate.opsForHash().putAll(key, sanitizedData);
            }
            expire(key);
        });
    }

    @Override
    public CacheLookupResult<Map<String, Object>> get(String gameId) {
        if (gameId == null || gameId.isBlank()) {
            return CacheLookupResult.miss();
        }

        String key = RedisKeyPrefix.GAME_SESSION.format(gameId);
        return cacheExecutor.executeRead(OP_GET, key, () -> {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries == null || entries.isEmpty()) {
                return CacheLookupResult.miss();
            }

            if (entries.size() == 1 && entries.containsKey(NULL_MARKER_FIELD)) {
                return CacheLookupResult.hitNull();
            }

            Map<String, Object> result = new HashMap<>();
            entries.forEach((entryKey, value) -> {
                if (entryKey != null && !Objects.equals(entryKey.toString(), NULL_MARKER_FIELD)) {
                    result.put(entryKey.toString(), value);
                }
            });

            if (result.isEmpty()) {
                return CacheLookupResult.miss();
            }
            return CacheLookupResult.hit(result);
        }, CacheLookupResult.miss());
    }

    @Override
    public void updateField(String gameId, String field, Object value) {
        if (gameId == null || gameId.isBlank() || field == null || field.isBlank()) {
            return;
        }

        String key = RedisKeyPrefix.GAME_SESSION.format(gameId);
        cacheExecutor.executeWrite(OP_UPDATE, key, () -> {
            if (value == null) {
                redisTemplate.opsForHash().delete(key, field);
            } else {
                redisTemplate.opsForHash().put(key, field, value);
            }
            redisTemplate.opsForHash().delete(key, NULL_MARKER_FIELD);
            expire(key);
        });
    }

    @Override
    public void updateStatus(String gameId, GameStatus status) {
        if (status == null) {
            return;
        }
        updateField(gameId, "status", status.name());
    }

    @Override
    public void evict(String gameId) {
        if (gameId == null || gameId.isBlank()) {
            return;
        }
        String key = RedisKeyPrefix.GAME_SESSION.format(gameId);
        cacheExecutor.executeWrite(OP_EVICT, key, () -> redisTemplate.delete(key));
    }

    private void expire(String key) {
        redisTemplate.expire(key, cacheProperties.getGameSessionTtlMinutes(), TimeUnit.MINUTES);
    }
}
