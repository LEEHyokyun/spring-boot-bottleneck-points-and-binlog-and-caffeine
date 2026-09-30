package com.order.controller;

import com.order.model.request.OrderUpdateRequest;
import com.order.model.response.OrderSelectResponse;
import com.order.model.response.OrderUpdateResponse;
import com.order.service.OrderCacheService;
import com.common.CacheStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final List<OrderCacheService> orderCacheServices;

    @GetMapping("/{cacheStrategy}/select")
    OrderSelectResponse select(
            @PathVariable CacheStrategy cacheStrategy,
            @RequestParam Long orderId
    ){
        return this.getOrderCacheService(cacheStrategy).select(orderId);
    }

    @PostMapping("/{cacheStrategy}/update")
    public OrderUpdateResponse update(
            @PathVariable CacheStrategy cacheStrategy,
            @RequestBody OrderUpdateRequest orderUpdateRequest
    ) {
        return this.getOrderCacheService(cacheStrategy).update(orderUpdateRequest);
    }

    private OrderCacheService getOrderCacheService(CacheStrategy cacheStrategy){
        return orderCacheServices.stream()
                .filter(orderCacheService -> orderCacheService.supports(cacheStrategy))
                .findFirst()
                .orElseThrow()
                ;
    }

}
