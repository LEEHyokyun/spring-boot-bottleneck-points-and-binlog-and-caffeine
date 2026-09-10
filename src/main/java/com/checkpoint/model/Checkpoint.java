package com.checkpoint.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "checkpoint")
@Getter
@NoArgsConstructor
public class Checkpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String binlogFile;
    private long binlogPosition;
    private String gtid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Checkpoint from(
            String binlogFile,
            long binlogPosition,
            String gtid
    ) {
        Checkpoint checkPoint = new Checkpoint();

        checkPoint.binlogFile = binlogFile;
        checkPoint.binlogPosition = binlogPosition;
        checkPoint.gtid = gtid;
        checkPoint.createdAt = LocalDateTime.now();
        checkPoint.updatedAt = checkPoint.createdAt;

        return checkPoint;
    }

}
