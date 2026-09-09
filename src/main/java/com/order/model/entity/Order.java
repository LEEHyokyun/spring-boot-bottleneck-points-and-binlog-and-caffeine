package com.order.model.entity;

import com.order.util.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void update(String orderStatus) {
        this.orderStatus = OrderStatus.to(orderStatus);
        this.updatedAt = LocalDateTime.now();
    }

//    public static Order create(Long userId, String orderStatus) {
//
//        Order order = new Order();
//
//        order.userId = userId;
//        order.orderStatus = OrderStatus.to(orderStatus);
//        order.createdAt = LocalDateTime.now();
//        order.updatedAt = order.createdAt;
//
//        return order;
//    }
}
