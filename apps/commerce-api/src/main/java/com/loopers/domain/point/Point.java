package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;

@Embeddable
public class Point {

    private static final long MINIMUM_INITIAL_POINT = 0L;
    private static final long MINIMUM_POSITIVE_POINT = 1L;
    private final Long pointValue;

    public Point() {
        this.pointValue = 0L;
    }

    public Point(Long pointValue) {
        validateInitialValue(pointValue);
        this.pointValue = pointValue;
    }

    public Point charge(Long amount) {
        validateChargeAmount(amount);
        return new Point(this.pointValue + amount);
    }

    public Point use(Long amount) {
        validateUseAmount(amount);
        return new Point(this.pointValue - amount);
    }

    public Long getPointValue() {
        return pointValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point otherPoint)) return false;
        return pointValue.equals(otherPoint.pointValue);
    }

    @Override
    public int hashCode() {
        return pointValue.hashCode();
    }

    @Override
    public String toString() {
        return pointValue.toString();
    }

    // =============================
    // 🔒 Validation methods
    // =============================

    private void validateInitialValue(Long value) {
        if (value == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 빈 값으로 생성될 수 없습니다.");
        }
        if (value < MINIMUM_INITIAL_POINT) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 음수로 생성될 수 없습니다.");
        }
    }

    private void validateChargeAmount(Long value) {
        if (value == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전할 포인트 값이 null입니다.");
        }
        if (value < MINIMUM_POSITIVE_POINT) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전할 포인트는 0보다 커야 합니다.");
        }
    }

    private void validateUseAmount(Long value) {
        if (value == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용할 포인트 값이 null입니다.");
        }
        if (value < MINIMUM_POSITIVE_POINT) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용할 포인트는 0보다 커야 합니다.");
        }
        if (this.pointValue < value) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }
    }
}
