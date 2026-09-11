package com.binlog.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class BinlogMetrics {

    private final MeterRegistry meterRegistry;

    /*
     * ------------------------------------------------------------
     * 1. Binlog Event 수신 횟수
     * ------------------------------------------------------------
     */
    private final Counter eventReceived;

    /*
     * ------------------------------------------------------------
     * 2. Binlog Event 처리 성공 횟수
     * ------------------------------------------------------------
     */
    private final Counter eventProcessed;

    /*
     * ------------------------------------------------------------
     * 3. Binlog Event 처리 실패 횟수
     * ------------------------------------------------------------
     */
    private final Counter eventFailed;

    /*
     * ------------------------------------------------------------
     * 4. Binlog Event 처리 시간
     *
     * 측정 범위 = One Transaction
     * BinaryLogClient Event 수신
     * BinlogEventFactory 에서 이벤트 변형
     * CaffeineSynchronizer 캐싱 동기화
     * Checkpoint Queue 적재 완료
     * ------------------------------------------------------------
     */
    private final Timer eventProcessTimer;

    /*
     * ------------------------------------------------------------
     * 5. Cache Synchronization 처리 시간
     *
     * 측정 범위 = 캐싱 동기화 및 큐 적재까지
     * CaffeineSynchronizer.synchronize() 캐싱 동기화
     * Caffeine PUT / EVICT
     * Checkpoint Queue 적재 완료
     * ------------------------------------------------------------
     */
    private final Timer cacheSyncTimer;

    /*
     * ------------------------------------------------------------
     * 6. Checkpoint 스케쥴러 실행 횟수
     * ------------------------------------------------------------
     */
    private final Counter checkpointBatchExecuted;

    /*
     * ------------------------------------------------------------
     * 7. Checkpoint 스케쥴러 DB 저장 시간
     * ------------------------------------------------------------
     */
    private final Timer checkpointBatchTimer;

    public BinlogMetrics(MeterRegistry meterRegistry) {

        this.meterRegistry = meterRegistry;

        eventReceived = Counter.builder("binlog_event_received_total")
                .description("Number of binlog events received")
                .register(meterRegistry);

        eventProcessed = Counter.builder("binlog_event_processed_total")
                .description("Number of binlog events successfully processed")
                .register(meterRegistry);

        eventFailed = Counter.builder("binlog_event_failed_total")
                .description("Number of binlog events failed")
                .register(meterRegistry);

        checkpointBatchExecuted = Counter.builder("binlog_checkpoint_batch_executed_total")
                .description("Number of checkpoint batch executions")
                .register(meterRegistry);

        eventProcessTimer = Timer.builder("binlog_event_process_seconds")
                .description("Time from binlog event reception to checkpoint queue insertion")
                .publishPercentileHistogram()
                .register(meterRegistry);

        cacheSyncTimer = Timer.builder("binlog_cache_sync_seconds")
                .description("Time from cache synchronization start to checkpoint queue insertion")
                .publishPercentileHistogram()
                .register(meterRegistry);

        checkpointBatchTimer = Timer.builder("binlog_checkpoint_batch_seconds")
                .description("Time spent persisting checkpoint batch")
                .publishPercentileHistogram()
                .register(meterRegistry);
    }

    /*
     * ------------------------------------------------------------
     * Event Received
     * ------------------------------------------------------------
     */
    public void incrementEventReceived() {
        eventReceived.increment();
    }

    /*
     * ------------------------------------------------------------
     * Event Processed
     * ------------------------------------------------------------
     */
    public void incrementEventProcessed() {
        eventProcessed.increment();
    }

    /*
     * ------------------------------------------------------------
     * Event Failed
     * ------------------------------------------------------------
     */
    public void incrementEventFailed() {
        eventFailed.increment();
    }

    /*
     * ------------------------------------------------------------
     * Event Process Timer 시작
     * ------------------------------------------------------------
     */
    public Timer.Sample startEventProcess() {
        return Timer.start(meterRegistry);
    }

    /*
     * ------------------------------------------------------------
     * Event Process Timer 종료
     * ------------------------------------------------------------
     */
    public void stopEventProcess(Timer.Sample sample) {
        sample.stop(eventProcessTimer);
    }

    /*
     * ------------------------------------------------------------
     * Cache Sync Timer 시작
     * ------------------------------------------------------------
     */
    public Timer.Sample startCacheSync() {
        return Timer.start(meterRegistry);
    }

    /*
     * ------------------------------------------------------------
     * Cache Sync Timer 종료
     * ------------------------------------------------------------
     */
    public void stopCacheSync(Timer.Sample sample) {
        sample.stop(cacheSyncTimer);
    }

    /*
     * ------------------------------------------------------------
     * Checkpoint Batch 실행
     * ------------------------------------------------------------
     */
    public void incrementCheckpointBatchExecuted() {
        checkpointBatchExecuted.increment();
    }

    /*
     * ------------------------------------------------------------
     * Checkpoint Batch Timer 시작
     * ------------------------------------------------------------
     */
    public Timer.Sample startCheckpointBatch() {
        return Timer.start(meterRegistry);
    }

    /*
     * ------------------------------------------------------------
     * Checkpoint Batch Timer 종료
     * ------------------------------------------------------------
     */
    public void stopCheckpointBatch(Timer.Sample sample) {
        sample.stop(checkpointBatchTimer);
    }
}
