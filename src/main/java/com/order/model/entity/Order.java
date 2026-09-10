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

    /*
    * Object[] rows -> Order
    * 현재 테이블 구조에 따라,
    *  id
    *  user_id
    *  product_id
    *  quantity
    *  price
    * 를 받으면,
    * row[0] → id
    * row[1] → user_id
    * row[2] → product_id
    * row[3] → quantity
    * row[4] → price
    * 의 형태로 Order 도메인 객체 정보를 얻을 수 있다.
    * */
    public static Order fromBinlogRow(Object[] row) {
        Order order = new Order();

        order.orderId = ((Number) row[0]).longValue();
        order.userId = ((Number) row[1]).longValue();
        order.orderStatus = OrderStatus.to(String.valueOf(row[2])) ;
        order.createdAt = toLocalDateTime(row[3]);
        order.updatedAt = toLocalDateTime(row[4]);

        return order;
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

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }

        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }

        if (value instanceof java.util.Date date) {
            return date.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime()
                    .withNano(0);
        }

        throw new IllegalArgumentException(
                "Unsupported datetime type: " + value.getClass()
        );
    }
}
