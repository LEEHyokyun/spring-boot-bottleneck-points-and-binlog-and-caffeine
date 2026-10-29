package com.order.service;

import com.order.model.entity.Order;
import com.order.model.request.OrderUpdateRequest;
import com.order.model.response.OrderSelectResponse;
import com.order.model.response.OrderUpdateResponse;
import com.order.repository.OrderRepository;
import com.order.util.OrderStatus;
import com.util.MySQLSliceTestContainerSupportUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest_Before {

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private OrderRepository orderRepository;

    @Test
    void orderServiceSelectTest() {
        // given
        Long orderId = 1L;
        Order order = Mockito.mock(Order.class);

        when(order.getOrderId()).thenReturn(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when
        OrderSelectResponse orderSelectResponse =
                orderService.select(orderId);

        // then
        assertNotNull(orderSelectResponse);
        assertEquals(orderId, orderSelectResponse.getOrderId());
        verify(orderRepository).findById(orderId);

    }

    @Test
    void orderServiceUpdateTest(){
        //given
        Long orderId = 1L;

        Order order = new Order(
                orderId,
                100L,
                OrderStatus.ORDERED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        OrderUpdateRequest orderUpdateRequest = Mockito.mock(OrderUpdateRequest.class);

        when(orderUpdateRequest.getOrderId()).thenReturn(orderId);
        when(orderUpdateRequest.getOrderStatus()).thenReturn(OrderStatus.PAID.getStatus1());
        when(orderRepository.getReferenceById(orderId)).thenReturn(order);

        //when
        OrderUpdateResponse orderUpdateResponse
                = orderService.update(orderUpdateRequest);


        //then
        assertNotNull(orderUpdateResponse);
        verify(orderRepository).getReferenceById(orderId);
        assertEquals(OrderStatus.PAID.getStatus1(), orderUpdateResponse.getOrderStatus());

    }

}