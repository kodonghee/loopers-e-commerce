package com.loopers.domain.order;

public enum OrderStatus {
    AWAITING_PAYMENT,      // 결제 대기
    PAID,                  // 결제 완료
    PAYMENT_DECLINED,       // 결제 실패 (비즈니스 이유: 잔액 부족, 카드번호 오류 등)
    PAYMENT_ERROR,        // 결제 실패 (시스템 이유: PG 장애, timeout 등)
    CANCELLED              // 주문 취소
}
