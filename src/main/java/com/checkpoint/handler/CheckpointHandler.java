package com.checkpoint.handler;

import com.binlog.event.BinlogEvent;
import com.binlog.event.BinlogPosition;
import com.checkpoint.model.Checkpoint;
import com.checkpoint.repository.CheckpointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckpointHandler {

    private final CheckpointRepository checkpointRepository;

    @Transactional
    public void saveAll(List<BinlogPosition> binlogPositions){

        /*
        * checkpoint를 저장할 내역이 없으면 그대로 종료.
        * */
        if (binlogPositions.isEmpty()) {
            return;
        }

        /*
        * 있다면 DB 영속화.
        * */
        List<Checkpoint> checkpoints =
                new ArrayList<>();

        for (BinlogPosition binlogPosition : binlogPositions) {

            checkpoints.add(
                    Checkpoint.from(
                            binlogPosition.binlogFile(),
                            binlogPosition.position(),
                            binlogPosition.gtid()
                    )
            );
        }

        checkpointRepository.saveAll(checkpoints);
    }

    /*
    * JVM 최초 시작이든 종료 후 복구이든
    * 마지막 CHECKPOINT 지점을 조회하여 복구한다.
    * 없다면 최초 시작, DB를 조회해서 캐싱 데이터로 적재한다.
    * */
    @Transactional(readOnly = true)
    public BinlogPosition loadLatest() {

        /*
        * JVM 최초 시작 및 JVM 중단 후 복구
        * 복구 시점에서 CHECKPOINT가 없으면 그대로 Optional.Empty이다.
        * 이는 예외처리가 아니라, 서버가 해당 position시점 이후부터 캐싱 동기화 및 영속화를 해야하는 위치를 알려주는 것임.
        * */
        return checkpointRepository.findFirstByOrderByIdDesc()
                .map(checkpoint -> new BinlogPosition(
                        checkpoint.getBinlogFile(),
                        checkpoint.getBinlogPosition(),
                        checkpoint.getGtid()
                        )
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "[ERROR][CheckpointHandler.loadLatest]Binlog Position 최초 상태가 존재하지 않습니다. 최초 상태를 적재하십시오."
                        )
                );
    }

}
