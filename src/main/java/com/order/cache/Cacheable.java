package com.order.cache;

import com.common.CacheDomain;
import com.common.CacheStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {
    CacheStrategy cacheStrategy() default CacheStrategy.CACHE_ASIDE;
    CacheDomain cacheDomain();
    String key();
    long ttl();
}
