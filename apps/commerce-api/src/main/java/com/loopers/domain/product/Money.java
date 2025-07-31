package com.loopers.domain.product;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class Money {

    private BigDecimal value;

    protected Money() {
    }

    public Money(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }
        this.value = amount;
    }

    public static Money from(int value) {
        return new Money(BigDecimal.valueOf(value));
    }

    public static Money from(BigDecimal value) {
        return new Money(value);
    }

    public BigDecimal getValue() {
        return value;
    }

    public Money plus(Money other) {
        return new Money(this.value.add(other.value));
    }

    public Money minus(Money other) {
        if (this.value.compareTo(other.value) < 0) {
            throw new IllegalArgumentException("차감할 수 없습니다.");
        }
        return new Money(this.value.subtract(other.value));
    }

    public Money multiply(int multiplier) {
        return new Money(this.value.multiply(BigDecimal.valueOf(multiplier)));
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.value.compareTo(other.value) >= 0;
    }

    public boolean isLessThan(Money other) {
        return this.value.compareTo(other.value) < 0;
    }

}
