package com.binlog.event;

import com.binlog.util.BinlogEventType;
import com.order.model.entity.Order;

/*
* debezium으로 받아 처리하는 이벤트 객체
* 처리의 효율화를 위해 별도 직렬화/역직렬화없이 그대로 객체화하여 사용한다.
* */
public record BinlogEvent(
    BinlogEventType type,
    Long orderId,
    Order order,
    BinlogPosition binlogPosition
) {
}
