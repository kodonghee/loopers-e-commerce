package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "member_point")
public class Point {

    private static final long MINIMUM_INITIAL_POINT = 0L;
    private static final long MINIMUM_POSITIVE_POINT = 1L;

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;
    private Long pointValue;

    protected Point() {
    }

    public Point(String userId, Long pointValue) {
        validateInitialValue(pointValue);
        this.userId = userId;
        this.pointValue = pointValue;
    }

    public void charge(Long amount) {
        validateChargeAmount(amount);
        this.pointValue += amount;
    }

    public void use(Long amount) {
        validateUseAmount(amount);
        this.pointValue -= amount;
    }

    public String getUserId() {
        return userId;
    }
    public Long getPointValue() {
        return pointValue;
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
