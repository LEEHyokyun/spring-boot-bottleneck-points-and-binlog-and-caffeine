package com.binlog.cache;

import com.binlog.event.BinlogPosition;
import com.checkpoint.handler.CheckpointHandler;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.order.model.entity.Order;
import com.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
* redisStringTemplate처럼 caffeine 전용 handler 생성
* OCP
* */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaffeineRecoveryHandler {

    private final CaffeineHandler caffeineHandler;
    private final CheckpointHandler checkpointHandler;
    private final OrderRepository orderRepository;

    /*
    * hot data 구간
    * */
    private static final long HOT_DATA_START_ORDER_ID = 1L;
    private static final long HOT_DATA_END_ORDER_ID = 200L;

    /*
    * Cache Recovery
    * 1. 최신 checkpoint 조회
    * 2. 현재 DB의 Hot Data 1~200 복구
    * 3. 조회한 checkpoint 반환
    * */
    public BinlogPosition recover(){
        /*
        * DB에 저장된 마지막 Checkpoint 조회
        * */
        BinlogPosition checkpoint = checkpointHandler.loadLatest();

        /*
        * 현재 DB에 저장된 Order 내역 전체를 캐싱 데이터로 복구
        * */
        restoreHotData();

        /*
        * 이 시점 이후부터는 binlogPosition 상태를 저장하여, 캐싱 데이터 동기화 및 position Replay가 이루어진다.
        * */
        log.info( "Cache recovery completed. cacheSize={}, checkpoint={}",
                caffeineHandler.size(), checkpoint
        );

        return checkpoint;
    }

    /*
    * 현재 DB의 Hot Data를 Caffeine에 복구한다.
    * checkpoint 존재 여부와 관계없이 항상 실행된다.
    * */
    private void restoreHotData(){

        /*
        * 현재 저장된 order 내역을 JVM Caffeine에 전체 복구
        * */
        List<Order> orders = orderRepository.findByOrderIdBetween(HOT_DATA_START_ORDER_ID, HOT_DATA_END_ORDER_ID);

        for (Order order : orders) {
            caffeineHandler.put( order.getOrderId(), order );
        }

        log.info( "Hot data restored. range={}~{}, count={}", HOT_DATA_START_ORDER_ID, HOT_DATA_END_ORDER_ID, orders.size() );

    }
}
