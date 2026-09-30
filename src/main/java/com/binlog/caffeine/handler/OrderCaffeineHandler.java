package com.binlog.caffeine.handler;

import com.binlog.metrics.BinlogMetrics;
import com.common.CacheDomain;
import com.common.CacheStrategy;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.common.KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

/*
* redisStringTemplate처럼 caffeine 전용 handler 생성
* OCP 잊지말자.
* */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCaffeineHandler implements  CaffeineHandler {

    private final KeyGenerator keyGenerator;
    private static final Long DEFAULT_TTL_SECONDS = 3000L; //TTL 정책 없으면 50분
    private final BinlogMetrics binlogMetrics;

    /*
    * 범용화(Long/Order 전용이 아니라 모든 AOP/동기화 처리에 사용 가능하도록
    * */
    public final Cache<String, CacheValue> cache =
            Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .expireAfter(new Expiry<String, CacheValue>() {

                        @Override
                        public long expireAfterCreate(
                                String key,
                                CacheValue value,
                                long currentTime
                        ) {
                            return value.ttlNanos();
                        }

                        @Override
                        public long expireAfterUpdate(
                                String key,
                                CacheValue value,
                                long currentTime,
                                long currentDuration
                        ) {
                            return value.ttlNanos();
                        }

                        @Override
                        public long expireAfterRead(
                                String key,
                                CacheValue value,
                                long currentTime,
                                long currentDuration
                        ) {
                            return currentDuration;
                        }
                    })
                    .build();
    /*
    * binlog / Recovery : 300sec default
    * */
    @Override
    public void put(CacheStrategy cacheStrategy, CacheDomain cacheDomain, String key, Object value) {

        cache.put(
                keyGenerator.generateOrderKey(cacheStrategy, cacheDomain, key),
                new CacheValue(
                    value,
                    Duration.ofSeconds(DEFAULT_TTL_SECONDS).toNanos()
                )
        );
    }

    /*
     * binlog / Recovery : 300sec default
     * */
    @Override
    public void put(CacheStrategy cacheStrategy, CacheDomain cacheDomain, long key, Object value) {

        cache.put(
                keyGenerator.generateOrderKey(cacheStrategy, cacheDomain, key),
                new CacheValue(
                        value,
                        Duration.ofSeconds(DEFAULT_TTL_SECONDS).toNanos()
                )
        );
    }

    @Override
    public void evict(CacheStrategy cacheStrategy, CacheDomain cacheDomain, long key){
        cache.invalidate(keyGenerator.generateOrderKey(cacheStrategy, cacheDomain, key));
    }

    @Override
    public Object get(CacheStrategy cacheStrategy, CacheDomain cacheDomain, String key){

        CacheValue cacheValue = cache.getIfPresent(keyGenerator.generateOrderKey(cacheStrategy, cacheDomain, key));

        return (cacheValue == null) ? null : cacheValue.value;
    }

    @Override
    public Object get(CacheStrategy cacheStrategy, CacheDomain cacheDomain, long key){

        CacheValue cacheValue = cache.getIfPresent(keyGenerator.generateOrderKey(cacheStrategy, cacheDomain, key));

        return (cacheValue == null) ? null : cacheValue.value;
    }

//    public Map<String, Object> snapshot() {
//
//        Map<String, Object> snapshot =
//                new HashMap<>();
//
//        cache.asMap().forEach(
//                (key, value) ->
//                        snapshot.put(
//                                key,
//                                value.value()
//                        )
//        );
//
//        return snapshot;
//    }

//    public void restore(Map<String, Object> snapshot) {
//        snapshot.forEach(
//                (key, value) ->
//                        cache.put(
//                                key,
//                                new CacheValue(
//                                        value,
//                                        DEFAULT_TTL
//                                )
//                        )
//        );
//    }

    @Override
    public long size(){
        return cache.estimatedSize();
    }

    /*
    * cacheaside : 300sec default
    * */
    @Override
    public Object fetch(
            CacheStrategy cacheStrategy,
            CacheDomain cacheDomain,
            String key,
            Duration ttl,
            Supplier<Object> supplier,
            Class<?> returnType
    ) {

        /*
        * 전체 요청 계측
        * */
        binlogMetrics.incrementCacheRequest();
        CacheValue cacheValue = cache.getIfPresent(keyGenerator.generateOrderKey(cacheStrategy, cacheDomain, key));

        /*
         * Cache Hit
         */
        if (cacheValue != null) {
            Object value = cacheValue.value();

            validateReturnType(
                    value,
                    returnType
            );

            return value;
        }

        /*
         * Cache Miss
         */

        Object value = supplier.get();

        /*
         * Cache Miss 계측
         * */
        binlogMetrics.incrementCacheMiss();

        validateReturnType(
                value,
                returnType
        );

        /*
         * Cache Miss -> TTL 적용한 캐싱 데이터 적재
         */
        cache.put(
                keyGenerator.generateOrderKey(cacheStrategy, cacheDomain, key),
                new CacheValue(
                        value,
                        ttl.toNanos()
                )
        );

        return value;
    }

    @Override
    public void validateReturnType(
            Object value,
            Class<?> returnType
    ) {
        if (value == null) {
            return;
        }

        if (!returnType.isInstance(value)) {
            throw new IllegalStateException(
                    "[ERROR][OrderCaffeineHandler.validateReturnType] Cached value type does not match return type. "
                            + "expected=" + returnType.getName()
                            + ", actual=" + value.getClass().getName()
            );
        }
    }

    @Override
    public boolean supports (CacheDomain cacheDomain) {
        return CacheDomain.ORDER == cacheDomain;
    }

    /*
    * caching Object 변환을 위한 nested class
    * */
    private record CacheValue(
            Object value,
            long ttlNanos
    ) {
    }

}
