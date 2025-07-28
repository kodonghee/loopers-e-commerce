package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;

@Embeddable
public class Point {

    private final Long pointValue;

    protected Point() {
        this.pointValue = 0L;
    }

    public Point(Long pointValue) {
        validateInitialValue(pointValue);
        this.pointValue = pointValue;
    }

    public Long getPointValue() {
        return pointValue;
    }
    public Point charge(Long amount) {
        validateChargeAmount(amount);
        return new Point(this.pointValue + amount);
    }

    public Point use(Long amount) {
        validateUseAmount(amount);
        return new Point(this.pointValue - amount);
    }

    // =============================
    // 🔒 Validation methods
    // =============================

    private void validateInitialValue(Long value) {
        if (value == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 빈 값으로 생성될 수 없습니다.");
        }
        if (value < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 음수로 생성될 수 없습니다.");
        }
    }

    private void validateChargeAmount(Long value) {
        if (value == null || value <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전할 포인트는 0보다 커야 합니다.");
        }
    }

    private void validateUseAmount(Long value) {
        if (value == null || value <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용할 포인트는 0보다 커야 합니다.");
        }
        if (this.pointValue < value) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }
    }
}
