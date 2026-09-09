package com.order.model.response;

import com.order.model.entity.Order;
import com.order.util.OrderStatus;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class OrderUpdateResponse {
    private Long orderId;
    private Long userId;
    private String orderStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderUpdateResponse from(Order order) {
        OrderUpdateResponse orderUpdateResponse = new OrderUpdateResponse();

        orderUpdateResponse.orderId = order.getOrderId();
        orderUpdateResponse.userId = order.getUserId();
        orderUpdateResponse.orderStatus = OrderStatus.from(order.getOrderStatus());
        orderUpdateResponse.createdAt = order.getCreatedAt();
        orderUpdateResponse.updatedAt = order.getUpdatedAt();

        return orderUpdateResponse;
    }
}
