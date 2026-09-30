package com.order.service;

import com.common.CacheDomain;
import com.order.cache.Cacheable;
import com.order.model.entity.Order;
import com.order.model.request.OrderUpdateRequest;
import com.order.model.response.OrderSelectResponse;
import com.order.model.response.OrderUpdateResponse;
import com.order.repository.OrderRepository;
import com.common.CacheStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCacheAsideService implements OrderCacheService{

    private final OrderRepository orderRepository;

    @Override
    @Cacheable(
            cacheStrategy = CacheStrategy.CACHE_ASIDE,
            cacheDomain = CacheDomain.ORDER,
            key = "#orderId",
            ttl = 3000
    )
    public OrderSelectResponse select(Long orderId){
        return OrderSelectResponse.from(orderRepository.findById(orderId).orElseThrow());
    }

    @Override
    @Transactional
    public OrderUpdateResponse update(OrderUpdateRequest orderUpdateRequest) {

        Order order = orderRepository.getReferenceById(orderUpdateRequest.getOrderId());
        order.update(orderUpdateRequest.getOrderStatus());

        return OrderUpdateResponse.from(order);
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy) { return CacheStrategy.CACHE_ASIDE == cacheStrategy; }
}
