package com.binlog.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.order.model.entity.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/*
* redisStringTemplate처럼 caffeine 전용 handler 생성
* OCP
* */
@Component
public class CaffeineHandler {

    private final Cache<Long, Order> cache = Caffeine.newBuilder().maximumSize(10_000).build();

    public void put(Long orderId, Order order) {
        cache.put(orderId, order);
    }

    public void evict(Long orderId){
        cache.invalidate(orderId);
    }

    public Order get(Long orderId){
        return cache.getIfPresent(orderId);
    }

    public Map<Long, Order> snapshot() {
        return new HashMap<>(cache.asMap());
    }

    public void restore(Map<Long, Order> snapshot) {
        cache.putAll(snapshot);
    }

    public long size(){
        return cache.estimatedSize();
    }

}
