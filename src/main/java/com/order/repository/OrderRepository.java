package com.order.repository;

import com.order.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    @NativeQuery("" +
            ""
    )
    List<Order> findByOrderIdBetween(
            Long startOrderId,
            Long endOrderId
    );
}
