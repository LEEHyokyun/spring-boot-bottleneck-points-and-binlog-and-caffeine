package com.order.service;

import com.order.model.request.OrderUpdateRequest;
import com.order.model.response.OrderSelectResponse;
import com.order.model.response.OrderUpdateResponse;
import com.common.CacheStrategy;

public interface OrderCacheService {

    public OrderSelectResponse select(Long orderId);

    public OrderUpdateResponse update(OrderUpdateRequest orderUpdateRequest);

    boolean supports(CacheStrategy cacheStrategy);
}
