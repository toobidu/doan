package org.example.quizizz.common.cache;

import lombok.Getter;

@Getter
public final class CacheLookupResult<T> {

    public enum Status {
        HIT,
        HIT_NULL,
        MISS
    }

    private final Status status;
    private final T value;

    private CacheLookupResult(Status status, T value) {
        this.status = status;
        this.value = value;
    }

    public static <T> CacheLookupResult<T> hit(T value) {
        return new CacheLookupResult<>(Status.HIT, value);
    }

    public static <T> CacheLookupResult<T> hitNull() {
        return new CacheLookupResult<>(Status.HIT_NULL, null);
    }

    public static <T> CacheLookupResult<T> miss() {
        return new CacheLookupResult<>(Status.MISS, null);
    }

    public boolean isHit() {
        return status == Status.HIT;
    }

    public boolean isHitNull() {
        return status == Status.HIT_NULL;
    }

    public boolean isMiss() {
        return status == Status.MISS;
    }
}
