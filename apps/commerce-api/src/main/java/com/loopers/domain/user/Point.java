package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;

@Embeddable
public class Point {

    private Long amount;

    protected Point() {
        this.amount = 0L;
    }

    public Point(Long amount) {
        this.amount = amount;
    }

    public Long getAmount() {
        return amount;
    }

    public void add(Long points) {
        if (points == null || points <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 포인트는 0 이하의 정수가 될 수 없습니다.");
        }
        this.amount += points;
    }

    public void deduct(Long points) throws IllegalAccessException {
        if (this.amount < points) {
            throw new IllegalAccessException("포인트가 부족합니다.");
        }
        this.amount -= points;
    }
}
