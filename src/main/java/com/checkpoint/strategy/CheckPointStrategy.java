package com.checkpoint.strategy;

import com.binlog.event.BinlogPosition;

import java.util.List;

public interface CheckPointStrategy {
    void save(BinlogPosition binlogPosition);
    List<BinlogPosition> load();
    List<BinlogPosition> peek(int size);
    void remove(int size);
    long count();
}
