package org.example.quizizz.common.cache.domain.game;

import org.example.quizizz.common.cache.CacheLookupResult;
import org.example.quizizz.common.constants.GameStatus;

import java.util.Map;

public interface GameSessionCache {

    void save(String gameId, Map<String, Object> sessionData);

    CacheLookupResult<Map<String, Object>> get(String gameId);

    void updateField(String gameId, String field, Object value);

    void updateStatus(String gameId, GameStatus status);

    void evict(String gameId);
}
