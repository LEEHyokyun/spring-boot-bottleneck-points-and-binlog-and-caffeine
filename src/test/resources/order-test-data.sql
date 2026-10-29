CREATE TABLE orders (
                        order_id BIGINT NOT NULL AUTO_INCREMENT,                                                   /* 주문내역 (FK) */
                        user_id BIGINT NOT NULL,                                                              /* 회원ID (FK) */
                        order_status VARCHAR(20) NOT NULL DEFAULT 'ORDERED',                                    /* 주문상태 */
    -- ordered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,                                 /* 주문일시 */
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,                                 /* 생성일시 */
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,     /* 수정일시 */

                        PRIMARY KEY (order_id)
    -- CONSTRAINT fk_orders_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    -- INDEX idx_order_status_ordered_at (order_status, ordered_at)
);


