package com.binlog.stream;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MySqlBinlogConsumer {

    @PostConstruct
    public void start(){

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
        * binlog event가 들어오면 handleEvent를 실행한다.
        * handleEvent = event 처리 형태(INSERT/UPDATE/DELETE)에 따라 이벤트 전송받아 처리
        * */
        client.registerEventListener(event -> this.handleEvent(event));

        /*
        * client를 구성하기 위해 별도의 비동기 스레드를 구성하여, 이 1개로 계속 점유한다.
        * */
        Thread thread = new Thread(() -> {
            try {
                log.info("MySQL Binlog connection connect tried");
                client.connect();
            } catch (Exception e) {
                log.error("MySQL Binlog connection failed", e);
            } finally {
                log.info("MySQL Binlog connection Information : " + client);
            }
        });

        /*
        * 별도 binlog client를 구성하기 위한 전용 스레드 설정.
        * */
        thread.setName("mysql-binlog-consumer");
        thread.start();

    }

    private void handleEvent(Event event){

        /*
        * binlog의 INSERT/UPDATE/DELETE 이벤트 별로 나누어서 이벤트를 Consume한다.
        * */
        Object data = event.getData();

        if (data instanceof WriteRowsEventData write) {

            log.info("INSERT: {}", write);

        } else if (data instanceof UpdateRowsEventData update) {

            log.info("UPDATE: {}", update);

        } else if (data instanceof DeleteRowsEventData delete) {

            log.info("DELETE: {}", delete);
        }

    }

}
