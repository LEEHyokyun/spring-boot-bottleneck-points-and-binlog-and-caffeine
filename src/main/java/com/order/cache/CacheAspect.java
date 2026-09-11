package com.order.cache;

import com.binlog.caffeine.CaffeineHandler;
import com.binlog.metrics.BinlogMetrics;
import com.order.util.KeyGenerator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {

    private final CaffeineHandler caffeineHandler;
    private final KeyGenerator keyGenerator;

    @Around("@annotation(cacheable)")
    public Object handleCacheable(
            ProceedingJoinPoint joinPoint,
            Cacheable cacheable
    ) {

        String key = keyGenerator.generateKey(
                joinPoint,
                cacheable.cacheName(),
                cacheable.key()
        );

        Duration ttl = Duration.ofSeconds(cacheable.ttl());

        Supplier<Object> supplier = createSupplier(joinPoint);

        Class<?> returnType = findReturnType(joinPoint);

        /*
        * handler -> 범용(String, Object)
        * */
        try {
            return caffeineHandler.fetch(
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
}
