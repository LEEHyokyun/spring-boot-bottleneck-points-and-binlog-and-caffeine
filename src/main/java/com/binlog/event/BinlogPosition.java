package com.binlog.event;

/*
* binlog 위치
* 즉, 현재 Caffeine Cache가 DB의 어느 binlog 시점까지 반영되어있는가
* Cache가 DB의 변경사항 중 어디까지 따라잡았는가
* mysql-bin.000001 : 10500 -> 해당 GTID까지 캐싱 동기화 완료.
* JVM 종료 시 이 시점 이후부터 캐싱 데이터를 복구.
* * position = 물리적인 binlog 위치
* * gtid = transaction을 한 위치, 지금까지 진행한 논리적인 위치
* */
public record BinlogPosition(
        String binlogFile, //해당 binlog 중에
        long position,  //여기까지 반영 성공
        String gtid  //JVM 다운 시 다음의 복구 지점은 여기부터
) {
}
