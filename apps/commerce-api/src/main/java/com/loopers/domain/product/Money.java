package com.loopers.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class Money {

    @Column(name = "price")
    private BigDecimal amount;

    protected Money() {
    }

    public Money(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

}
