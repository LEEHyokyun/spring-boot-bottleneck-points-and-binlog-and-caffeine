package com.binlog.consumer;

import com.binlog.event.BinlogPosition;
import com.checkpoint.handler.CheckpointHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MySqlBinlogConsumerTest {

    @Autowired
    private CheckpointHandler checkpointHandler;

    @Autowired
    private MySqlBinlogConsumer mySqlBinlogConsumer;

    @Test
    void startTest(){
        //given
        String binlogFile = "binlog.000003";
        long binlogPosition = 4;


        BinlogPosition checkPoint = checkpointHandler.loadLatest();

        //when
        mySqlBinlogConsumer.start(checkPoint);

        //then
        assertEquals(binlogFile, checkPoint.binlogFile());
        assertEquals(binlogPosition, checkPoint.position());

    }

}