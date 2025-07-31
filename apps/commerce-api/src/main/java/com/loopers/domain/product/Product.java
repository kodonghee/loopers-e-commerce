package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private final String name;
    private int stock;
    private final int price;
    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    public Product(String name, int stock, int price) {
        if (stock < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }

        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감 수량은 0보다 커야 합니다.");
        }
        if (this.stock < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        this.stock -= quantity;
    }

    public int getStock() {
        return stock;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }
}
