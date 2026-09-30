package com.check;

import com.binlog.caffeine.handler.OrderCaffeineHandler;
import com.common.CacheDomain;
import com.common.CacheStrategy;
import com.order.model.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
public class CheckController {

    private final OrderCaffeineHandler orderCaffeineHandler;

    @GetMapping("/get/{orderId}")
    public Order getOrder(
            @PathVariable Long orderId) {
        return Order.from( orderCaffeineHandler.get(CacheStrategy.CACHE_ASIDE, CacheDomain.ORDER, orderId) );
    }
}
