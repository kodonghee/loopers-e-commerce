package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "member_point")
public class Point {

    // BigDecimal 상수 사용
    private static final BigDecimal MINIMUM_INITIAL_POINT = BigDecimal.ZERO;
    private static final BigDecimal MINIMUM_POSITIVE_POINT = BigDecimal.ONE;

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;
    private BigDecimal pointValue;

    protected Point() {
    }

    public Point(String userId, BigDecimal pointValue) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID must not be null or empty.");
        }
        validateInitialValue(pointValue);
        this.userId = userId;
        this.pointValue = pointValue;
    }

    public void charge(BigDecimal amount) {
        validateChargeAmount(amount);
        this.pointValue = this.pointValue.add(amount);
    }

    public void use(BigDecimal amount) {
        validateUseAmount(amount);
        this.pointValue = this.pointValue.subtract(amount);
    }

    public String getUserId() {
        return userId;
    }
    public BigDecimal getPointValue() {
        return pointValue;
    }

    // =============================
    // 🔒 Validation methods
    // =============================

    private void validateInitialValue(BigDecimal value) {
        if (value == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 빈 값으로 생성될 수 없습니다.");
        }

        if (value.compareTo(MINIMUM_INITIAL_POINT) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 음수로 생성될 수 없습니다.");
        }
    }

    private void validateChargeAmount(BigDecimal value) {
        if (value == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전할 포인트 값이 null입니다.");
        }

        if (value.compareTo(MINIMUM_POSITIVE_POINT) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전할 포인트는 0보다 커야 합니다.");
        }
    }

    private void validateUseAmount(BigDecimal value) {
        if (value == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용할 포인트 값이 null입니다.");
        }

        if (value.compareTo(MINIMUM_POSITIVE_POINT) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용할 포인트는 0보다 커야 합니다.");
        }

        if (this.pointValue.compareTo(value) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Objects.equals(userId, point.userId) && Objects.equals(pointValue, point.pointValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, pointValue);
    }
}
