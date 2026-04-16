package org.example.quizizz.common.cache.domain.presence;

public interface OnlineUserCache {

    void markOnline(Long userId);

    void markOffline(Long userId);
}
