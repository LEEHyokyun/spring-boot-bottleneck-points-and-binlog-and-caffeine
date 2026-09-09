package com.order.model.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class OrderUpdateRequest {
    private Long orderId;
    private String orderStatus;
}
