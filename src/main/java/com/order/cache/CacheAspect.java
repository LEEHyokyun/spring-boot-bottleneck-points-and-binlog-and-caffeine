package com.order.cache;

import com.binlog.caffeine.handler.CaffeineHandler;
import com.common.CacheDomain;
import com.common.KeyGenerator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {

    private final List<CaffeineHandler> caffeineHandlers;
    private final KeyGenerator keyGenerator;

    @Around("@annotation(cacheable)")
    public Object handleCacheable(
            ProceedingJoinPoint joinPoint,
            Cacheable cacheable
    ) {

        String key = keyGenerator.generateKey(
                joinPoint,
                cacheable.cacheStrategy(),
                cacheable.cacheDomain(),
                cacheable.key()
        );

        Duration ttl = Duration.ofSeconds(cacheable.ttl());

        Supplier<Object> supplier = createSupplier(joinPoint);

        Class<?> returnType = findReturnType(joinPoint);

        /*
        * handler -> 범용(String, Object)
        * */
        try {
            return this.getCaffeineHandler(cacheable.cacheDomain()).fetch(
                    cacheable.cacheStrategy(),
                    cacheable.cacheDomain(),
                    key,
                    ttl,
                    supplier,
                    returnType
            );
        } catch (Exception e) {
            return supplier.get();
        }
    }

    private Supplier<Object> createSupplier(
            ProceedingJoinPoint joinPoint
    ) {
        return () -> {

            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Class<?> findReturnType(JoinPoint joinPoint) {
        MethodSignature signature =
                (MethodSignature) joinPoint.getSignature();

        return signature.getReturnType();
    }

    private CaffeineHandler getCaffeineHandler(CacheDomain cacheDomain) {
        return caffeineHandlers.stream()
                .filter(caffeineHandler -> caffeineHandler.supports(cacheDomain))
                .findFirst()
                .orElseThrow()
                ;
    }
}
