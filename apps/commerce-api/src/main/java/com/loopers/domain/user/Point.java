package com.loopers.domain.user;

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
        this.amount += points;
    }

    public void deduct(Long points) throws IllegalAccessException {
        if (this.amount < points) {
            throw new IllegalAccessException("포인트가 부족합니다.");
        }
        this.amount -= points;
    }
}
