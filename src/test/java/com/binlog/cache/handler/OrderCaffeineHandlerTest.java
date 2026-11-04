package com.binlog.cache.handler;

import com.binlog.caffeine.handler.CaffeineHandler;
import com.binlog.caffeine.handler.OrderCaffeineHandler;
import com.common.CacheDomain;
import com.common.CacheStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.support.WebContentGenerator;

import java.time.Duration;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderCaffeineHandlerTest {

    @Autowired
    private OrderCaffeineHandler orderCaffeineHandler;

    private final CacheStrategy cacheStrategy = CacheStrategy.CACHE_ASIDE;
    private final CacheDomain cacheDomain = CacheDomain.ORDER;
    private final String key = "order-test";
    private final long longKey = 1L;
    private WebContentGenerator webContentGenerator;

    /*
    * 테스트 결과 초기화 > 각 테스트의 독립적인 실행
    * */
    @BeforeEach
    void setUp() {
        orderCaffeineHandler.cache.invalidateAll();
        orderCaffeineHandler.cache.cleanUp();
    }

    @Test
    void putTest(){
        //given
        String value = "ORDERED";

        //when
        orderCaffeineHandler.put(
                cacheStrategy,
                cacheDomain,
                key,
                value
        );

        //then
        assertEquals(value, orderCaffeineHandler.get(cacheStrategy, cacheDomain, key));
    }

    @Test
    void evictTest(){
        //given
        String value = "ORDERED";

        orderCaffeineHandler.put(
                cacheStrategy,
                cacheDomain,
                longKey,
                value
        );

        //when
        orderCaffeineHandler.evict(
                cacheStrategy,
                cacheDomain,
                longKey
        );

        //then
        assertNull(
                orderCaffeineHandler.get(
                        cacheStrategy, cacheDomain, longKey
                )
        );
    }

    @Test
    void getTest(){
        //given
        String value = "ORDERED";

        orderCaffeineHandler.put(
                cacheStrategy,
                cacheDomain,
                key,
                value
        );

        //when
        Object result = orderCaffeineHandler.get(
                cacheStrategy, cacheDomain, key
        );


        //then
        assertEquals(value, result);
    }

    @Test
    void sizeTest(){
        //given
        String value1 = "ORDERED";
        String value2 = "PAID";
        String key1 = "order-test-1";
        String key2 = "order-test-2";

        orderCaffeineHandler.put(
                cacheStrategy,
                cacheDomain,
                key1,
                value1
        );

        orderCaffeineHandler.put(
                cacheStrategy,
                cacheDomain,
                key2,
                value2
        );

        //when
        long size = orderCaffeineHandler.size();

        //then
        assertEquals(2, size);
    }

    @Test
    void fetchCacheMissTest(){
        //given
        String value = "ORDERED";

        Supplier<Object> supplier = Mockito.mock(Supplier.class);
        when(supplier.get()).thenReturn("ORDERED");

        //when
        Object result = orderCaffeineHandler.fetch(
                cacheStrategy,
                cacheDomain,
                key,
                Duration.ofSeconds(10),
                supplier,
                String.class
        );

        //then
        assertEquals("ORDERED", result);
        verify(supplier, times(1)).get();
    }

    @Test
    void fetchCacheHitTest(){
        //given
        String value = "ORDERED";
        Supplier<Object> supplier = Mockito.mock(Supplier.class);

        orderCaffeineHandler.put(
                cacheStrategy,
                cacheDomain,
                key,
                value
        );

        //when
        Object result = orderCaffeineHandler.fetch(
                cacheStrategy,
                cacheDomain,
                key,
                Duration.ofSeconds(10),
                supplier,
                String.class
        );


        //then
        assertEquals(value, result);
        verify(supplier, never()).get();

    }

    @DisplayName("캐싱 대상 value와 명시된 타입이 서로 일치해야 한다.")
    @Test
    void validateReturnType(){

        /*
        * 캐싱대상(value)와 해당 대상의 타입 캐스팅이 서로 일치해야한다.
        * */

        //when/then
        assertThrows(
                IllegalStateException.class,
                () -> orderCaffeineHandler.validateReturnType(
                        100,
                        String.class
                )
        );
    }

}