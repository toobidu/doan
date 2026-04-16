package org.example.quizizz.common.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.cache.redis")
public class RedisCacheProperties {

    private boolean enabled = true;
    private long userPermissionsTtlHours = 24;
    private long gameSessionTtlMinutes = 120;
    private long onlineUsersTtlHours = 24;
}
