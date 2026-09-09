package com.order.model.response;

import com.order.model.entity.Order;
import com.order.util.OrderStatus;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class OrderSelectResponse {
    private Long orderId;
    private Long userId;
    private String orderStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderSelectResponse from(Order order) {
        OrderSelectResponse orderSelectResponse = new OrderSelectResponse();

        orderSelectResponse.orderId = order.getOrderId();
        orderSelectResponse.userId = order.getUserId();
        orderSelectResponse.orderStatus = OrderStatus.from(order.getOrderStatus());
        orderSelectResponse.createdAt = order.getCreatedAt();
        orderSelectResponse.updatedAt = order.getUpdatedAt();

        return orderSelectResponse;
    }
}
