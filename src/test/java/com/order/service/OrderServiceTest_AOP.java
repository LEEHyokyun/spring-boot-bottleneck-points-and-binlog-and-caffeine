package com.order.service;

import com.order.cache.CacheAspect;
import com.order.cache.Cacheable;
import com.order.model.entity.Order;
import com.order.model.response.OrderSelectResponse;
import com.order.repository.OrderRepository;
import com.order.util.OrderStatus;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest_AOP {

    @Autowired
    private OrderCacheAsideService orderCacheAsideService;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoSpyBean
    private CacheAspect cacheAspect;

    @Test
    void aopPassingByTest() {
        //given
        Long orderId = 100L;

        Order order = new Order(
                orderId,
                1L,
                OrderStatus.ORDERED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        //when
        OrderSelectResponse orderSelectResponse
                = orderCacheAsideService.select(orderId);


        //then
        verify(cacheAspect, times(1)).handleCacheable(
            any(ProceedingJoinPoint.class),
            any(Cacheable.class)
        );
    }

}