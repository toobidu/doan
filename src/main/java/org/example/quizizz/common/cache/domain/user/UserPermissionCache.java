package org.example.quizizz.common.cache.domain.user;

import org.example.quizizz.common.constants.PermissionCode;

import java.util.Set;

public interface UserPermissionCache {

    void save(Long userId, Set<PermissionCode> permissions);

    Set<PermissionCode> get(Long userId);

    void evict(Long userId);
}
