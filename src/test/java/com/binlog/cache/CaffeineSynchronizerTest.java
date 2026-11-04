package com.binlog.cache;

import com.binlog.caffeine.CaffeineSynchronizer;
import com.binlog.caffeine.handler.OrderCaffeineHandler;
import com.binlog.event.BinlogEvent;
import com.binlog.event.BinlogPosition;
import com.binlog.util.BinlogEventType;
import com.common.CacheDomain;
import com.common.CacheStrategy;
import com.order.model.entity.Order;
import com.order.util.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CaffeineSynchronizerTest {

    @Autowired
    private CaffeineSynchronizer caffeineSynchronizer;

    @Autowired
    private OrderCaffeineHandler orderCaffeineHandler;

    private BinlogEvent createBinlogEvent(){
        return new BinlogEvent(
                BinlogEventType.INSERT,
                1L,
                new Order(
                        1L,
                        1L,
                        OrderStatus.ORDERED,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ),
                new BinlogPosition(
                        "binlog.000003",
                        4,
                        ""
                )
        );
    }

    private BinlogEvent updateBinlogEvent(){
        return new BinlogEvent(
                BinlogEventType.INSERT,
                1L,
                new Order(
                        1L,
                        1L,
                        OrderStatus.PAID,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ),
                new BinlogPosition(
                        "binlog.000003",
                        4,
                        ""
                )
        );
    }

    @Test
    void synchronizeInsertTest() {

        //given
        BinlogEvent binlogEvent = createBinlogEvent();

        //when
        caffeineSynchronizer.synchronize(binlogEvent);

        Order order = (Order) orderCaffeineHandler.get(
                CacheStrategy.CACHE_ASIDE,
                CacheDomain.ORDER,
                1L
        );

        //then
        assertEquals(OrderStatus.ORDERED, order.getOrderStatus());
        assertEquals(1L, order.getOrderId());
    }

    @Test
    void synchronizeUpdateTest() {

        //given
        BinlogEvent binlogEvent = createBinlogEvent();
        binlogEvent = updateBinlogEvent();

        //when
        caffeineSynchronizer.synchronize(binlogEvent);

        Order order = (Order) orderCaffeineHandler.get(
                CacheStrategy.CACHE_ASIDE,
                CacheDomain.ORDER,
                1L
        );

        //then
        assertEquals(OrderStatus.PAID, order.getOrderStatus());
        assertEquals(1L, order.getOrderId());
    }
}