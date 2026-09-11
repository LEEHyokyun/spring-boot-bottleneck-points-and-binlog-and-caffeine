package com.binlog.cache;

import com.binlog.metrics.BinlogMetrics;
import com.checkpoint.strategy.CheckPointStrategy;
import com.binlog.event.BinlogEvent;
import com.exception.CacheSynchronizationException;
import com.order.model.entity.Order;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaffeineSynchronizer {

    private final BinlogMetrics binlogMetrics;

    private final CaffeineHandler caffeineHandler;

    /*
    * checkPoint를 어디에 저장할 것인가
    * 현재 체크포인트 기억 및 처리 방법이 계속 바뀌어서 유지관리/확장성을 위해 전략패턴으로 도입한다.
    * 추가적으로 현재는 Caffeine이지만, 별도 파일에 저장할 수도 있지 않을까.
    * */
    private final CheckPointStrategy checkPointStrategy;

    /*
    * binlog event에 따른 캐싱 데이터 동기화
    * */
    public void synchronize(BinlogEvent binlogEvent){

        Timer.Sample sample = binlogMetrics.startCacheSync();

        /*
        * binlogEvent를 받아 캐싱 데이터를 동기화한다.
        * 캐싱 데이터를 동기화하는 지점은 Caffeine 반영이 성공한 이후에만.
        * */
        try {
            switch (binlogEvent.type()) {

                case INSERT -> handleInsert(binlogEvent);

                case UPDATE -> handleUpdate(binlogEvent);

                case DELETE -> handleDelete(binlogEvent);

            }

            /*
             * 캐싱 데이터 동기화 + 현재 동기화한 binlog의 위치(position)를 기억한다.
             * Caffeine Cache 반영이 성공한 경우에만
             * 해당 Binlog Position을 checkpoint로 기록한다.
             * 즉, 캐싱 데이터 소실 시 캐싱 데이터 복구할때 해당 point 지점이 복구 대상이 될 수 있음.
             */
            checkPointStrategy.save(binlogEvent.binlogPosition());

        } catch (Exception e) {

            /*
             * Cache 반영에 실패했다면 checkpoint를 갱신하지 않는다.
             * Cache 반영에 실패했다는 것은 JVM에 오류가 발생하거나, 다운되었다는 의미.
             * 따라서 재시작 시 마지막으로 성공한 checkpoint 이후부터
             * 다시 binlog를 읽을 수 있다.
             */
            log.error(
                    "Failed to synchronize binlog event. type={}, orderId={}, position={}",
                    binlogEvent.type(),
                    binlogEvent.orderId(),
                    binlogEvent.binlogPosition(),
                    e
            );

            throw new CacheSynchronizationException(
                    "Failed to synchronize binlog event: " + binlogEvent,
                    e
            );

            /*
            * 추가 : 캐싱에 실패했는데 JVM은 계속 동작중이라면?
            * -> 캐싱 동기화가 이루어지지 않는다. 그대신 binlog에 쌓이므로 이를 이벤트로 받아서 복구 처리가 가능하다.
            * */
        } finally {
            binlogMetrics.stopCacheSync(sample);
        }

    }

    /*
    * event type에 따라 캐싱처리.
    * Event로부터 Order 객체는 따로 얻는다.
    * */
    private void handleInsert(BinlogEvent event) {

        Order order = getOrder(event);

        caffeineHandler.put(order.getOrderId(), order);
    }

    private void handleUpdate(BinlogEvent event) {

        Order order = getOrder(event);

        caffeineHandler.put(order.getOrderId(), order);
    }

    private void handleDelete(BinlogEvent event) {

        /*
        * 삭제의 경우 캐싱에서 삭제한다.
        * */
        caffeineHandler.evict(event.orderId());
    }

    /*
    * event로부터 order 도메인 객체를 얻는다.
    * */
    private Order getOrder(BinlogEvent event) {

        if (event.order() == null) {
            throw new IllegalStateException(
                    "[ERROR][CaffeineSynchrnizer.getOrder] Order 도메인 객체를 추출할 수 없습니다. " + event.type()
            );
        }

        return event.order();
    }
}
