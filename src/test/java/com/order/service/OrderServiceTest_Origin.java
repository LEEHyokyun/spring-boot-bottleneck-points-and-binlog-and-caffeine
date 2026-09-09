package com.order.service;

import com.order.model.entity.Order;
import com.order.model.request.OrderUpdateRequest;
import com.order.model.response.OrderUpdateResponse;
import com.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest_Origin {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private Order order;

    @InjectMocks
    private OrderService orderService;

    private OrderUpdateRequest request;

    @BeforeEach
    void setUp() {

        request = mock(OrderUpdateRequest.class);

        when(request.getOrderId())
                .thenReturn(1L);

        when(request.getOrderStatus())
                .thenReturn(null);

        when(orderRepository.getReferenceById(1L))
                .thenReturn(order);
    }

    @Test
    void sliceTest() {

        // when
        OrderUpdateResponse response =
                orderService.update(request);

        // then

        // Repository에서 Order를 조회했는지 검증
        verify(orderRepository)
                .getReferenceById(1L);

        // Order의 상태 변경 메서드가 호출되었는지 검증
        verify(order)
                .update(null);

        // Response가 정상적으로 생성되었는지 검증
        assertNotNull(response);
    }
}