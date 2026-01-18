package com.loopers.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Money {

    @Column(name = "price")
    private final BigDecimal amount;

    protected Money() {
        this.amount = BigDecimal.ZERO;
    }

    public Money(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;

        // BigDecimal은 equals() 대신 compareTo()를 사용해야 함
        // 1.0과 1.00을 equals()는 다르다고 판단하지만, compareTo()는 같다고 판단함
        return amount != null && amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        // hashCode는 값의 정규화가 필요할 수 있으나,
        // 일반적으로는 amount의 소수점 단위를 통일하거나 아래와 같이 사용함
        return Objects.hash(amount.stripTrailingZeros());
    }

    public BigDecimal getAmount() {
        return amount;
    }

}
