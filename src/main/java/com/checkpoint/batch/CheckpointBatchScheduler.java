package com.checkpoint.batch;

import com.checkpoint.strategy.CheckPointStrategy;
import com.binlog.event.BinlogPosition;
import com.checkpoint.handler.CheckpointHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckpointBatchScheduler {

    /*
    * 1분마다 DB에 적재하는 크기 = 100
    * */
    private static final int CHUNK_SIZE = 100;

    private final CheckPointStrategy checkPointStrategy;
    private final CheckpointHandler checkpointHandler;

    /* * 1분마다 최대 100개의 In-Memory Checkpoint를 * DB에 Insert한다(No batch, 스케쥴링으로 처리). */
    @Scheduled(fixedRate = 60_000)
    public void persist() {
        List<BinlogPosition> checkpoints = checkPointStrategy.peek(CHUNK_SIZE); /* * 저장할 checkpoint가 없으면 종료 */ if (checkpoints.isEmpty()) { log.info( "Checkpoint batch skipped. queue is empty." ); return; }
        log.info( "Checkpoint batch started. count={}, queueSize={}", checkpoints.size(), checkPointStrategy.count() );

        try {
            /* * 먼저 DB에 저장한다. * * 아직 In-Memory Queue에서는 제거하지 않는다. */
            checkpointHandler.saveAll(checkpoints);

            /* * DB 저장 성공 후에만 Queue에서 제거한다(오류 발생 시 queue에 남아있는것부터 다시 시작하면 됨). */
            checkPointStrategy.remove( checkpoints.size() );
            log.info( "Checkpoint batch completed. count={}, remainingQueueSize={}", checkpoints.size(), checkPointStrategy.count()
            );

        } catch (Exception e) {
            /* * DB 저장 실패 * * remove()를 호출하지 않았기 때문에 * 해당 checkpoint들은 In-Memory Queue에 그대로 남아 있다. */
            log.error( "Checkpoint batch failed. count={}, queueSize={}", checkpoints.size(), checkPointStrategy.count()
                    , e
            );
        }
    }

}
