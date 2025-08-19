package com.loopers.domain.order;

public enum OrderStatus {
    CREATED,               // 주문 생성
    AWAITING_PAYMENT,      // 결제 대기
    PAID,                  // 결제 완료
    PAYMENT_FAILED         // 결제 실패
}
