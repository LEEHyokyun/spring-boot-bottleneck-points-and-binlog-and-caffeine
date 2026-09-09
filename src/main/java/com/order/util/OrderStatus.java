
package com.order.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    ORDERED("ORDERED", "주문완료"),
    PAID("PAID", "결제완료"),
    NO_STATUS("NONE", "상태없음")
    ;

    private final String status1;
    private final String status2;

    public static OrderStatus to(String status1){

        for (OrderStatus orderStatus : values()) {
            if (orderStatus.status1.equals(status1)) {
                return orderStatus;
            }
        }

        log.error("[ERROR][TimeSelections.from] No attendance options found={}", status1);
        return OrderStatus.NO_STATUS;

    }

    public static String from(OrderStatus status){
        try {
            return status.getStatus1();
        } catch (Exception e) {
            log.error("[OrderStatus.from] status={}", status, e);
            return null;
        }
    }
}
