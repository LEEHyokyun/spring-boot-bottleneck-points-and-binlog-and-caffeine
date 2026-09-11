package com.binlog.caffeine;

import com.binlog.consumer.MySqlBinlogConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaffeineRecoveryCoordinator {

    private final CaffeineRecoveryHandler caffeineRecoveryHandler;
    private final MySqlBinlogConsumer mySqlBinlogConsumer;

    /**
    * JVM 시작
    * 1. 최신 checkpoint 조회
    * 2. Hot Data 1~200 복구 (이건 default)
    * 3. checkpoint를 Binlog Consumer에 전달(binlogPosition부터 다시 캐싱 데이터 동기화 및 적재)
    **/
    @EventListener(ApplicationReadyEvent.class)
    public void recover() {
        mySqlBinlogConsumer.start(
                caffeineRecoveryHandler.recover()
        );
    }

}
