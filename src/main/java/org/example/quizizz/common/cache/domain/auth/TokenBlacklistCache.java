package org.example.quizizz.common.cache.domain.auth;

public interface TokenBlacklistCache {

    void blacklist(String token, long refreshTokenExpiration);

    boolean isBlacklisted(String token);
}
