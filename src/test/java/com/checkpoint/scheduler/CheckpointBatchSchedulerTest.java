package com.checkpoint.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CheckpointBatchSchedulerTest {

    @Autowired
    private CheckpointBatchScheduler checkpointBatchScheduler;

    @Test
    void SchedulerTest(){
        checkpointBatchScheduler.persist();
    }

}