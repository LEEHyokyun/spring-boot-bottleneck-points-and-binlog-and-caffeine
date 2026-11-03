package com.binlog.event;

import com.binlog.util.BinlogEventType;
import com.github.shyiko.mysql.binlog.event.*;
import com.order.model.entity.Order;
import com.order.util.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BinlogEventFactoryTest {

    @Autowired
    private BinlogEventFactory binlogEventFactory;

    private Event createEvent(EventData eventData) {

        EventHeaderV4 eventHeaderV4 = new EventHeaderV4();
        eventHeaderV4.setNextPosition(100);

        return new Event(
                eventHeaderV4,
                eventData
        );
    }

    private List<Serializable[]> createRows(){
        long orderId = 1;
        long userId = 1;
        String orderStatus = "ORDERED";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt;

        List<Serializable[]> rows = new ArrayList<>();
        rows.add(new Serializable[]{
                orderId,
                userId,
                orderStatus,
                createdAt,
                updatedAt
        });

        return rows;
    }

    private List<Map.Entry<Serializable[], Serializable[]>> createRowsForUpdate(){
        long orderId = 1;
        long userId = 1;
        String orderStatusBefore = "ORDERED";
        String orderStatusAfter = "PAID";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt;

        Serializable[] beforeRow = {
                orderId,
                userId,
                orderStatusBefore,
                createdAt,
                updatedAt
        };

        Serializable[] afterRow = {
                orderId,
                userId,
                orderStatusAfter,
                createdAt,
                LocalDateTime.now()
        };

        List<Map.Entry<Serializable[], Serializable[]>> rows = new ArrayList<>();
        rows.add(new AbstractMap.SimpleEntry<>(beforeRow, afterRow));

        return rows;
    }

    @Test
    void createBinlogEventTest(){

        //given
        long orderId = 1L;

        BinlogEventType insertType = BinlogEventType.INSERT;
        OrderStatus orderStatus = OrderStatus.ORDERED;
        List<Serializable[]> rows = createRows();

        WriteRowsEventData eventData = new WriteRowsEventData();
        eventData.setRows(rows);

        Event event = createEvent(eventData);

        String binlogFileName = "binlog.000003";
        String gtid = "";

        BinlogPosition binlogPosition =
                new BinlogPosition(binlogFileName, 100L, gtid);

        //when
        BinlogEvent binlogEvent = binlogEventFactory.createBinlogEvent(
                event,
                binlogFileName,
                gtid
        ).get();

        //then
        assertEquals(insertType, binlogEvent.type());
        assertEquals(orderId, binlogEvent.orderId());
        assertEquals(orderStatus, binlogEvent.order().getOrderStatus());
        assertEquals(binlogPosition, binlogEvent.binlogPosition());

    }

    @Test
    void updateBinlogEventTest(){

        //given
        long orderId = 1L;

        BinlogEventType updateType = BinlogEventType.UPDATE;
        OrderStatus orderStatus = OrderStatus.PAID;
        List<Map.Entry<Serializable[], Serializable[]>> rows = createRowsForUpdate();

        UpdateRowsEventData eventData = new UpdateRowsEventData();
        eventData.setRows(rows);

        Event event = createEvent(eventData);

        String binlogFileName = "binlog.000003";
        String gtid = "";

        BinlogPosition binlogPosition =
                new BinlogPosition(binlogFileName, 100L, gtid);

        //when
        BinlogEvent binlogEvent = binlogEventFactory.createBinlogEvent(
                event,
                binlogFileName,
                gtid
        ).get();

        //then
        assertEquals(updateType, binlogEvent.type());
        assertEquals(orderId, binlogEvent.orderId());
        assertEquals(orderStatus, binlogEvent.order().getOrderStatus());
        assertEquals(binlogPosition, binlogEvent.binlogPosition());

    }

    @Test
    void deleteBinlogEventTest(){

        //given
        long orderId = 1L;

        BinlogEventType deleteType = BinlogEventType.DELETE;
        List<Serializable[]> rows = createRows();

        DeleteRowsEventData eventData = new DeleteRowsEventData();
        eventData.setRows(rows);

        Event event = createEvent(eventData);

        String binlogFileName = "binlog.000003";
        String gtid = "";

        BinlogPosition binlogPosition =
                new BinlogPosition(binlogFileName, 100L, gtid);

        //when
        BinlogEvent binlogEvent = binlogEventFactory.createBinlogEvent(
                event,
                binlogFileName,
                gtid
        ).get();

        //then
        assertEquals(deleteType, binlogEvent.type());
        assertEquals(orderId, binlogEvent.orderId());
        assertEquals(null, binlogEvent.order());
        assertEquals(binlogPosition, binlogEvent.binlogPosition());

    }

}