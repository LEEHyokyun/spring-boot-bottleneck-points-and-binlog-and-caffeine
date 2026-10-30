package com.checkpoint.handler;

import com.binlog.event.BinlogPosition;
import com.checkpoint.model.Checkpoint;
import com.checkpoint.repository.CheckpointRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CheckpointHandlerTest {

    @Autowired
    private CheckpointHandler checkpointHandler;

    @Autowired //실제 동작 + 최종 상태까지 검증
    private CheckpointRepository checkpointRepository;

    @Test
    void saveAllTest(){

        //given
        long beforeCount = checkpointRepository.count();

        List<BinlogPosition> binlogPositions = List.of(
                new BinlogPosition(
                        "binlog-test-000001",
                        100L,
                        "gtid-test-0000001"
                ),
                new BinlogPosition(
                        "binlog-test-000002",
                        200L,
                        "gtid-test-0000002"
                )
        );

        //when
        checkpointHandler.saveAll(binlogPositions);

        //then
        List<Checkpoint> checkpoints = checkpointRepository.findAll();
        assertEquals(beforeCount + binlogPositions.size(), checkpoints.size());

    }

    /*
    * 최초 상태를 기준으로 binlogPosition 상태 확인
    * 반드시 별도 테스트 진행
    * */
    @Test
    void loadLatestTest(){

        //given
        String binlogFile = "binlog.000003";
        long binlogPosition = 4L;

        //when
        BinlogPosition initialPosition = checkpointHandler.loadLatest();

        //then
        assertEquals(binlogFile, initialPosition.binlogFile());
        assertEquals(binlogPosition, initialPosition.position());

    }
}