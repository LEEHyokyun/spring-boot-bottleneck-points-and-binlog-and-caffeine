package com.binlog.caffeine;

import com.binlog.metrics.BinlogMetrics;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.order.model.entity.Order;
import com.order.util.KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/*
* redisStringTemplate처럼 caffeine 전용 handler 생성
* OCP 잊지말자.
* */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaffeineHandler {

    private final KeyGenerator keyGenerator;
    private static final Long DEFAULT_TTL_SECONDS = 3000L; //TTL 정책 없으면 50분
    private final BinlogMetrics binlogMetrics;

    /*
    * 범용화(Long/Order 전용이 아니라 모든 AOP/동기화 처리에 사용 가능하도록
    * */
    private final Cache<String, CacheValue> cache =
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
    public void put(String key, Object value) {

        cache.put(
                keyGenerator.generateOrderKey(key),
                new CacheValue(
                    value,
                    Duration.ofSeconds(DEFAULT_TTL_SECONDS).toNanos()
                )
        );
    }

    /*
     * binlog / Recovery : 300sec default
     * */
    public void put(long key, Object value) {

        cache.put(
                keyGenerator.generateOrderKey(key),
                new CacheValue(
                        value,
                        Duration.ofSeconds(DEFAULT_TTL_SECONDS).toNanos()
                )
        );
    }


    public void evict(long key){
        cache.invalidate(keyGenerator.generateOrderKey(key));
    }

    public Object get(String key){

        CacheValue cacheValue = cache.getIfPresent(keyGenerator.generateOrderKey(key));

        return (cacheValue == null) ? null : cacheValue.value;
    }

    public Object get(long key){

        CacheValue cacheValue = cache.getIfPresent(keyGenerator.generateOrderKey(key));

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

    public long size(){
        return cache.estimatedSize();
    }

    /*
    * cacheaside : 300sec default
    * */
    public Object fetch(
            String key,
            Duration ttl,
            Supplier<Object> supplier,
            Class<?> returnType
    ) {

        /*
        * 전체 요청 계측
        * */
        binlogMetrics.incrementCacheRequest();
        CacheValue cacheValue = cache.getIfPresent(keyGenerator.generateOrderKey(key));

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
                keyGenerator.generateOrderKey(key),
                new CacheValue(
                        value,
                        ttl.toNanos()
                )
        );

        return value;
    }

    private void validateReturnType(
            Object value,
            Class<?> returnType
    ) {
        if (value == null) {
            return;
        }

        if (!returnType.isInstance(value)) {
            throw new IllegalStateException(
                    "[ERROR][CaffeineHandler.validateReturnType] Cached value type does not match return type. "
                            + "expected=" + returnType.getName()
                            + ", actual=" + value.getClass().getName()
            );
        }
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
