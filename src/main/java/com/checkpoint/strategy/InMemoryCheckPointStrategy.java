package com.checkpoint.strategy;

import com.binlog.event.BinlogPosition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@RequiredArgsConstructor
public class InMemoryCheckPointStrategy implements CheckPointStrategy {

    /*
    * 동시적으로 발생하는 요청들의 순서를 보장해야 함
    * 이벤트 순서대로 캐싱 데이터에 반영해야 하므로
    * */
    private final Queue<BinlogPosition> binlogPositions = new ConcurrentLinkedQueue<>();

    /*
    * 체크포인트 갱신
    * */
    @Override
    public void save(BinlogPosition binlogPosition) {

        binlogPositions.add(binlogPosition);

        log.info(
                "Binlog checkpoint updated. file={}, position={}, gtid={}",
                binlogPosition.binlogFile(),
                binlogPosition.position(),
                binlogPosition.gtid()
        );

    }

    /*
    * 현재 메모리에 존재하는 checkpoint 모두 복사해서 조회해온다(조회).
    * */
    @Override
    public List<BinlogPosition> load() {
        return new ArrayList<>(binlogPositions);
    }

    /*
    * 현재까지 쌓인 checkpoint 정보를 추출하고, 메모리에서 제거한다(갱신).
    * - 배치전용!
    * */
    @Override
    public List<BinlogPosition> peek(int size) {

        List<BinlogPosition> checkpoints = new ArrayList<>(size);

        int count = 0;

        for (BinlogPosition binlogPosition : binlogPositions) {
            checkpoints.add(binlogPosition); count++;

            if (count == size) break;

        } return checkpoints;
    }

    /* *
    * DB 영속화가 성공한 checkpoint만 Queue에서 제거한다.
    * 유실 방지 필수
    * * */
    @Override public void remove(int size) {
        for (int i = 0; i < size; i++) {
            if (binlogPositions.poll() == null)  break;
        }
    }

    /*
     * 현재까지 적재된 CHECKPOINT 대상의 크기
     */
    @Override
    public long count() {
        return binlogPositions.size();
    }

}
