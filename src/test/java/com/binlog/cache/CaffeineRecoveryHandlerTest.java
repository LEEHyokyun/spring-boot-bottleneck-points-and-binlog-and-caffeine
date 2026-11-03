package com.binlog.cache;

import com.binlog.caffeine.CaffeineRecoveryHandler;
import com.binlog.event.BinlogPosition;
import com.checkpoint.handler.CheckpointHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CaffeineRecoveryHandlerTest {

    @Autowired
    private CaffeineRecoveryHandler caffeineRecoveryHandler;

    @Test
    void recoverTest(){
        //given
        String binlog_file = "binlog.000003";
        long binlog_position = 4;

        //when
        BinlogPosition binlogPosition = caffeineRecoveryHandler.recover();

        //then
        assertEquals(binlog_file, binlogPosition.binlogFile());
        assertEquals(binlog_position, binlogPosition.position());
    }

}