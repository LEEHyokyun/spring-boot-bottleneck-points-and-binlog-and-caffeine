package com.order.service;

import com.order.cache.Cacheable;
import com.order.model.entity.Order;
import com.order.model.request.OrderSelectRequest;
import com.order.model.request.OrderUpdateRequest;
import com.order.model.response.OrderSelectResponse;
import com.order.model.response.OrderUpdateResponse;
import com.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Cacheable(
            cacheName = "order",
            key = "#orderId",
            ttl = 300
    )
    public OrderSelectResponse select(Long orderId){
        return OrderSelectResponse.from(orderRepository.findById(orderId).orElseThrow());
    }

    @Transactional
    public OrderUpdateResponse update(OrderUpdateRequest orderUpdateRequest) {

        Order order = orderRepository.getReferenceById(orderUpdateRequest.getOrderId());
        order.update(orderUpdateRequest.getOrderStatus());

        return OrderUpdateResponse.from(order);
    }
}
