package com.binlog.event;

import com.binlog.util.BinlogEventType;
import com.github.shyiko.mysql.binlog.event.*;
import com.order.model.entity.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BinlogEventFactory {

    /*
    * binlog event type에 따라
    * Order 객체, BinlogPosition 객체, BinlogEvent 객체를 생성한다.
    * 도메인 객체를 추출해주는 역할(즉, 변환해주는 Factory의 책임)
    * event + factory = BinlogEvent + BinlogPosition
    * */
    public Optional<BinlogEvent> createBinlogEvent(Event event, String binlogFileName, String gtid) {

        Object data = event.getData();

        if(data instanceof WriteRowsEventData writeRowsEventData) {
            return Optional.of(createInsert(writeRowsEventData, event, binlogFileName, gtid));
        }

        if (data instanceof UpdateRowsEventData updateRowsEventData) {
            return Optional.of(createUpdate(updateRowsEventData, event, binlogFileName, gtid));
        }

        if (data instanceof DeleteRowsEventData deleteRowsEventData) {
            return Optional.of(createDelete(deleteRowsEventData, event, binlogFileName, gtid));
        }

        return Optional.empty();

    }

    /*
    * 1) event로부터 orderId 추출
    * 2) event로부터 order 객체 추출
    * 3) checkpoint 정보
    * */

    private BinlogEvent createInsert(
            WriteRowsEventData eventData,
            Event event,
            String binlogFileName,
            String gtid
    ) {
        Long orderId = extractOrderId(eventData);

        Order order = extractOrder(eventData);

        BinlogPosition position = extractBinlogPosition(event, binlogFileName, gtid);

        return new BinlogEvent(
                BinlogEventType.INSERT,
                orderId,
                order,
                position
        );
    }

    private BinlogEvent createUpdate(
            UpdateRowsEventData eventData,
            Event event,
            String binlogFileName,
            String gtid
    ) {
        Long orderId = extractOrderId(eventData);

        Order order = extractAfterOrder(eventData);

        BinlogPosition position = extractBinlogPosition(event, binlogFileName, gtid);

        return new BinlogEvent(
                BinlogEventType.UPDATE,
                orderId,
                order,
                position
        );
    }

    private BinlogEvent createDelete(
            DeleteRowsEventData eventData,
            Event event,
            String binlogFileName,
            String gtid
    ) {
        Long orderId = extractOrderId(eventData);

        BinlogPosition position = extractBinlogPosition(event, binlogFileName, gtid);

        return new BinlogEvent(
                BinlogEventType.DELETE,
                orderId,
                null,
                position
        );
    }

    /*
    * event data로부터 orderId 추출
    * - event payload는 before, after 두개가 존재한다.
    * */
    private Long extractOrderId(WriteRowsEventData eventData) {
        Object[] row = eventData.getRows().get(0);
        return ((Number) row[0]).longValue();
    }

    private Long extractOrderId(UpdateRowsEventData eventData) {
        Object[] beforeRow = eventData.getRows().get(0).getKey();
        return ((Number) beforeRow[0]).longValue();
    }

    private Long extractOrderId(DeleteRowsEventData eventData) {
        Object[] row = eventData.getRows().get(0);
        return ((Number) row[0]).longValue();
    }

    /*
    * binlog row는 Object[] row 형태로 들어오는데,
    * 이 row 중에 Order 객체 정보만을 추출하기 위해 Order 도메인 객체 책임 하에 변환이 필요하다.
    * */
    private Order extractOrder(WriteRowsEventData eventData) {
        Object[] row = eventData.getRows().get(0);

        //Object[] -> Order
        return Order.fromBinlogRow(row);
    }

    private Order extractAfterOrder(UpdateRowsEventData eventData) {
        Object[] afterRow = eventData.getRows().get(0).getValue();

        return Order.fromBinlogRow(afterRow);
    }

    /*
    * 마찬가지로 binlogPosition 정보(현재 시점에서 Checkpoint 정보)를 event에서 추출한다.
    * eventHeader로부터 다음 binlog position을 나타내는 값이 들어있다.
    * */
    private BinlogPosition extractBinlogPosition(Event event, String binlogFileName, String gtid) {
        return new BinlogPosition(
                binlogFileName,
                ((EventHeaderV4) event.getHeader()).getNextPosition(),
                gtid
        );
    }

}
