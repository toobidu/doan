package org.example.quizizz.common.cache.redis;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.example.quizizz.common.cache.config.RedisCacheProperties;
import org.example.quizizz.common.cache.exception.CacheSerializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.serializer.SerializationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisCacheExecutorTest {

    private RedisCacheProperties properties;
    private RedisCacheExecutor executor;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        properties = new RedisCacheProperties();
        meterRegistry = new SimpleMeterRegistry();
        executor = new RedisCacheExecutor(properties, meterRegistry);
    }

    @Test
    void executeReadReturnsFallbackWhenCacheDisabled() {
        properties.setEnabled(false);

        String value = executor.executeRead("GET", "k1", () -> "value", "fallback");

        assertEquals("fallback", value);
    }

    @Test
    void executeReadReturnsFallbackOnConnectionFailure() {
        String value = executor.executeRead(
                "GET",
                "k2",
                () -> {
                    throw new RedisConnectionFailureException("redis down");
                },
                "fallback"
        );

        assertEquals("fallback", value);
    }

    @Test
    void executeWriteThrowsCacheSerializationException() {
        assertThrows(CacheSerializationException.class, () -> executor.executeWrite(
                "PUT",
                "k3",
                () -> {
                    throw new SerializationException("cannot serialize");
                }
        ));
    }

    @Test
    void executeReadThrowsCacheSerializationException() {
        assertThrows(CacheSerializationException.class, () -> executor.executeRead(
                "GET",
                "k4",
                () -> {
                    throw new SerializationException("cannot deserialize");
                },
                "fallback"
        ));
    }
}
