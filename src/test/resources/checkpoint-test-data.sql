CREATE TABLE checkpoint (
                            id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                            binlog_file VARCHAR(255) NOT NULL,
                            binlog_position BIGINT NOT NULL,
                            gtid TEXT NULL,
                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,                                 /* 생성일시 */
                            updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP      /* 수정일시 */
);


INSERT INTO checkpoint (
    binlog_file,
    binlog_position,
    gtid,
    created_at,
    updated_at
)
VALUES (
           'binlog.000003',
           4,
           '',
           NOW(),
           NOW()
);
