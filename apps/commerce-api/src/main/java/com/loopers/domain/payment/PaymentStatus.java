package com.loopers.domain.payment;

public enum PaymentStatus {
    PENDING,  // 결제 요청 대기
    SUCCESS,  // 결제 성공
    DECLINED, // 비즈니스 거절 (잔액 부족, 카드 오류 등)
    ERROR;    // 시스템 장애 (PG 장애, Timeout 등)

    public boolean isFinalized() {
        return this != PENDING;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFailure() {
        return this == DECLINED || this == ERROR;
    }

    public static PaymentStatus fromPgStatus(String pgStatus) {
        return switch (pgStatus) {
            case "SUCCESS" -> SUCCESS;
            case "FAILED" -> DECLINED;
            default -> ERROR;
        };
    }
}
