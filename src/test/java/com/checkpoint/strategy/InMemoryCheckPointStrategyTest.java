package com.checkpoint.strategy;

import com.binlog.event.BinlogPosition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class InMemoryCheckPointStrategyTest {

    private final CheckPointStrategy inMemoryCheckPointStrategy = new InMemoryCheckPointStrategy();

    private BinlogPosition createBinlogPosition(long position){
        return new BinlogPosition(
                "binlog-test-00003",
            position,
            ""
        );
    }

    @Test
    void saveTest(){

        //given
        BinlogPosition binlogPosition1 = createBinlogPosition(1);
        BinlogPosition binlogPosition2 = createBinlogPosition(2);

        //when
        inMemoryCheckPointStrategy.save(binlogPosition1);
        inMemoryCheckPointStrategy.save(binlogPosition2);

        //then
        assertEquals(2, inMemoryCheckPointStrategy.count());
        assertEquals(binlogPosition1, inMemoryCheckPointStrategy.load().getFirst());

    }

    @Test
    void loadTest(){
        //given
        BinlogPosition binlogPosition1 = createBinlogPosition(1);
        BinlogPosition binlogPosition2 = createBinlogPosition(2);

        inMemoryCheckPointStrategy.save(binlogPosition1);
        inMemoryCheckPointStrategy.save(binlogPosition2);

        //when
        List<BinlogPosition> binlogPositions = inMemoryCheckPointStrategy.load();

        //then
        assertEquals(2, binlogPositions.size());
        assertEquals(binlogPosition1, binlogPositions.getFirst());

    }

    @Test
    void peekTest(){

        //given
        BinlogPosition binlogPosition1 = createBinlogPosition(1);
        BinlogPosition binlogPosition2 = createBinlogPosition(2);

        inMemoryCheckPointStrategy.save(binlogPosition1);
        inMemoryCheckPointStrategy.save(binlogPosition2);

        List<BinlogPosition> binlogPositions = inMemoryCheckPointStrategy.load();

        //when
        List<BinlogPosition> peeked = inMemoryCheckPointStrategy.peek(2);

        //then
        assertEquals(2, peeked.size());
        assertEquals(binlogPosition1, peeked.getFirst());

    }

    @Test
    void removeTest(){

        //given
        BinlogPosition binlogPosition1 = createBinlogPosition(1);
        BinlogPosition binlogPosition2 = createBinlogPosition(2);

        inMemoryCheckPointStrategy.save(binlogPosition1);
        inMemoryCheckPointStrategy.save(binlogPosition2);

        //when
        inMemoryCheckPointStrategy.remove(2);

        //then
        assertEquals(0, inMemoryCheckPointStrategy.count());

    }

    @Test
    void countTest(){

        //given
        BinlogPosition binlogPosition1 = createBinlogPosition(1);
        BinlogPosition binlogPosition2 = createBinlogPosition(2);

        //when
        inMemoryCheckPointStrategy.save(binlogPosition1);
        inMemoryCheckPointStrategy.save(binlogPosition2);

        //then
        assertEquals(2, inMemoryCheckPointStrategy.count());

    }
}