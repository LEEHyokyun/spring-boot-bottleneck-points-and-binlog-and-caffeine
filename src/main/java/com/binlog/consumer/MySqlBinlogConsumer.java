package com.binlog.consumer;

import com.binlog.cache.CaffeineSynchronizer;
import com.binlog.event.BinlogEventFactory;
import com.binlog.event.BinlogPosition;
import com.binlog.metrics.BinlogMetrics;
import com.checkpoint.handler.CheckpointHandler;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MySqlBinlogConsumer {

    private final BinlogMetrics binlogMetrics;

    /*
    * 초기값 지정 : JVM 최초 시작을 위해 최초로 설정한 값
    * */
    private final String INITIAL_BINLOG_FILE = "binlog.000003";
    private final long INITIAL_BINLOG_POSITION = 4;
    private final String INITIAL_GTID = "";

    private final BinlogEventFactory binlogEventFactory;
    private final CaffeineSynchronizer caffeineSynchronizer;
    private final CheckpointHandler checkpointHandler;

    /* consuming 하는 테이블 대상은 Orders 한개, 나머지는 필터링..이건 event에서 추출한다. */
    private Long orderTableId;

    /*
    * DB Checkpoint를 읽은 뒤 그 위치에서 복구하고 시작.
    * */
    public void start(BinlogPosition checkpoint){

        /*
        * binlog 별도 connection
        * */
        BinaryLogClient client = new BinaryLogClient(
                "localhost",
                3306,
                "binlog_consumer",
                "password"
        );

        /*
        * 해당 client가 binlog의 이벤트를 listen하여 이벤트를 전송받도록 구성
        * */
        client.setServerId(100);

        /*
        * 마지막에 DB에 영속된 checkpoint부터 binlog를 읽어온다.
        * 그리고 캐싱동기화 및 binlog 적재 시작
        * */
        if (checkpoint != null) {
            client.setBinlogFilename(checkpoint.binlogFile());
            client.setBinlogPosition(checkpoint.position());

            log.info(
                "MySQL Binlog consumer starts from checkpoint. file={}, position={}, gtid={}", checkpoint.binlogFile(), checkpoint.position(), checkpoint.gtid()
            );
        }
        else {
            /* * 최초 시작 시 Binlog 시작 위치는
            * * 별도의 초기화 위치가 필요하다.
            * * * 현재 Consumer에는 checkpoint가 없으므로
            * * 여기서 임의의 position을 설정하지 않는다(없으면 없는대로 처음부터 진행하는 것).
            * */
            client.setBinlogFilename(
                    INITIAL_BINLOG_FILE
            );

            client.setBinlogPosition(
                    INITIAL_BINLOG_POSITION
            );

            log.info(
                    "MySQL Binlog consumer starts from initial position. file={}, position={}",
                    INITIAL_BINLOG_FILE,
                    INITIAL_BINLOG_POSITION
            );
        }

        /*
        * MySQL binlog Replication 연결해서 JVM이 살아있는 동안 계속 읽는다.
        * CHECPOINT 기반, binlog를 읽으면서 누락된 복구 및 동기화 등을 진행하는 것.
        * */
        client.registerEventListener( event -> {
            if (isOrderEvent(event)) {
                //handleEvent(client, event);

                binlogMetrics.incrementEventReceived();

                var sample = binlogMetrics.startEventProcess();

                try {

                    /*
                    * handleEvent
                    * */
                    handleEvent(client, event);

                    binlogMetrics.incrementEventProcessed();

                } catch (Exception e) {

                    binlogMetrics.incrementEventFailed();

                    throw e;

                } finally {

                    binlogMetrics.stopEventProcess(sample);
                }
            }
        });

        /*
        * MySQL Binlog에 연결해서 체크포인트에 따라 캐싱 동기화 및 복구 작업을 수행하는 주체
        * Tomcat Thread랑 달리 비동기적으로 따로 움직인다.
        * */
        Thread thread = new Thread( () -> connect(client) );
        thread.setName( "mysql-binlog-consumer" );
        thread.start();

    }

    private void connect(BinaryLogClient client) {
        try {
            log.info("MySQL Binlog connection connect tried");
            client.connect();
        } catch (Exception e) {
            log.error("MySQL Binlog connection failed", e);
        } finally {
            log.info("MySQL Binlog connection Information : " + client);
        }
    }

    private void handleEvent(BinaryLogClient client, Event event){

        log.info(
                "BINLOG EVENT RECEIVED: type={}, file={}, position={}",
                event.getHeader().getEventType(),
                client.getBinlogFilename(),
                event.getData()
        );

        /*
        * binlog의 raw event를 Factory를 통해 BinlogEvent로 반환받는다.
        * 이 반환받은 Event 객체가 존재한다면, Caffeine에 캐싱한다.
        * */
        binlogEventFactory
                /*
                * 최종 checkpoint 기억 장소 = InMemoryCheckPointStrategy의 BinlogPosition
                * */
                .createBinlogEvent(event, client.getBinlogFilename(), client.getGtidSet())
                .ifPresent(binlogEvent -> caffeineSynchronizer.synchronize(binlogEvent));

    }

    /* order Event에 대해서만 처리한다. */
    private boolean isOrderEvent(Event event) {

        Object data = event.getData();

        if (data instanceof TableMapEventData tableMap) {
            orderTableId = getOrderTableId(tableMap);
            return false;
        }

        if (orderTableId == null) {
            return false;
        }

        /* id를 별도 추출해야 하는 이유는 각 binlogEvent는 table 이름을 포함하지 않기 때문이다. */
        if (data instanceof WriteRowsEventData write) {
            return write.getTableId() == orderTableId;
        }

        if (data instanceof UpdateRowsEventData update) {
            return update.getTableId() == orderTableId;
        }

        if (data instanceof DeleteRowsEventData delete) {
            return delete.getTableId() == orderTableId;
        }

        /* order event가 아니라면 싹다 consuming 대상에서 반려 */
        return false;
    }

    private Long getOrderTableId(TableMapEventData tableMap) {

        if ("orders".equalsIgnoreCase(tableMap.getTable())) {
            return tableMap.getTableId();
        }

        return null;
    }
}
