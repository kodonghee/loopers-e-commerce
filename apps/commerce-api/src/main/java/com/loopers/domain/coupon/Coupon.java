package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "member_coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @Enumerated(EnumType.STRING)
    private CouponType type;

    private BigDecimal amount;

    private boolean used;

    protected Coupon() {}

    public Coupon(String userId, CouponType type, BigDecimal amount) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.used = false;
    }

    public Long getId() { return id; }

    public String getUserId() { return userId; }

    public boolean isUsed() { return used; }

    public BigDecimal getAmount() { return amount; }

    public BigDecimal applyCoupon(BigDecimal totalAmount) {
        if (used) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }

        BigDecimal discount = switch (type) {
            case FIXED -> amount.min(totalAmount);
            case RATE -> totalAmount.multiply(amount).divide(new BigDecimal("100"), 0, RoundingMode.FLOOR);
        };

        return totalAmount.subtract(discount);
    }

    public void markAsUsed() {
        if (used) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }

        this.used = true;
    }
}
