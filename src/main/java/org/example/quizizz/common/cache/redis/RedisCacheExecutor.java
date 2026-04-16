package org.example.quizizz.common.cache.redis;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.quizizz.common.cache.config.RedisCacheProperties;
import org.example.quizizz.common.cache.exception.CacheSerializationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheExecutor {

    private static final String METRIC_NAME = "quizizz.cache.redis.operations";

    private final RedisCacheProperties cacheProperties;
    private final MeterRegistry meterRegistry;

    public boolean isEnabled() {
        return cacheProperties.isEnabled();
    }

    public <T> T executeRead(String operation, String key, Supplier<T> action, T fallback) {
        if (!isEnabled()) {
            log.debug("Cache disabled, skip read. op={}, key={}", operation, key);
            recordMetric(operation, "disabled");
            return fallback;
        }

        try {
            T value = action.get();
            recordMetric(operation, "success");
            return value;
        } catch (SerializationException e) {
            recordMetric(operation, "serialization_error");
            throw newSerializationException(operation, key, e);
        } catch (RedisConnectionFailureException | RedisSystemException | QueryTimeoutException e) {
            log.error("Redis read failed, fallback to default. op={}, key={}", operation, key, e);
            recordMetric(operation, "connection_error_fallback");
            return fallback;
        } catch (Exception e) {
            log.error("Unexpected cache read error, fallback to default. op={}, key={}", operation, key, e);
            recordMetric(operation, "unexpected_error_fallback");
            return fallback;
        }
    }

    public void executeWrite(String operation, String key, Runnable action) {
        if (!isEnabled()) {
            log.debug("Cache disabled, skip write. op={}, key={}", operation, key);
            recordMetric(operation, "disabled");
            return;
        }

        try {
            action.run();
            recordMetric(operation, "success");
        } catch (SerializationException e) {
            recordMetric(operation, "serialization_error");
            throw newSerializationException(operation, key, e);
        } catch (RedisConnectionFailureException | RedisSystemException | QueryTimeoutException e) {
            log.error("Redis write failed, continue without cache. op={}, key={}", operation, key, e);
            recordMetric(operation, "connection_error_ignored");
        } catch (Exception e) {
            log.error("Unexpected cache write error, continue without cache. op={}, key={}", operation, key, e);
            recordMetric(operation, "unexpected_error_ignored");
        }
    }

    private CacheSerializationException newSerializationException(String operation, String key, SerializationException e) {
        log.error("Cache serialization failed. op={}, key={}", operation, key, e);
        return new CacheSerializationException(
                "Cache serialization failed for operation " + operation + " and key " + key,
                e
        );
    }

    private void recordMetric(String operation, String outcome) {
        Counter.builder(METRIC_NAME)
                .tag("operation", sanitize(operation))
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value;
    }
}
