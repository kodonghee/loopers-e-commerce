package com.loopers.domain.product;

import jakarta.persistence.Embeddable;

@Embeddable
public class Stock {

    private int stockValue;

    protected Stock() {
    }

    public Stock(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        }
        this.stockValue = value;
    }

    public Stock decrease(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("재고 차감량은 1 이상이어야 합니다.");
        }
        if (stockValue < quantity) {
            throw new IllegalArgumentException("재고가 부족 합니다.");
        }
        return new Stock(this.stockValue - quantity);
    }

    public int getValue() {
        return stockValue;
    }
}

