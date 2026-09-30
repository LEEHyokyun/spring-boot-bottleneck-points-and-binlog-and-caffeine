package com.binlog.caffeine.handler;

import com.common.CacheDomain;
import com.common.CacheStrategy;

import java.time.Duration;
import java.util.function.Supplier;

public interface CaffeineHandler {

    Object fetch(
            CacheStrategy cacheStrategy,
            CacheDomain cacheDomain,
            String key,
            Duration ttl,
            Supplier<Object> supplier,
            Class<?> returnType
    );

    void put(
            CacheStrategy cacheStrategy,
            CacheDomain cacheDomain,
            String key,
            Object value
    );

    void put(
            CacheStrategy cacheStrategy,
            CacheDomain cacheDomain,
            long key,
            Object value
    );

    void evict(CacheStrategy cacheStrategy, CacheDomain cacheDomain, long key);

    Object get(CacheStrategy cacheStrategy, CacheDomain cacheDomain, String key);

    Object get(CacheStrategy cacheStrategy, CacheDomain cacheDomain, long key);

    long size();

    void validateReturnType(Object value, Class<?> returnType);

    boolean supports(
            CacheDomain cacheDomain
    );
}
