package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Embedded
    private Stock stock;

    @Embedded
    private Money price;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    protected Product() {
    }

    public Product(String name, Stock stock, Money price, Long brandId) {
        validateName(name);
        validateBrandId(brandId);
        this.name = name;
        this.stock = stock;
        this.price = price;
        this.brandId = brandId;
    }

    public String getName() { return name; }

    public Stock getStock() { return stock; }

    public Money getPrice() { return price; }

    public Long getBrandId() { return brandId; }

    public void decreaseStock(int quantity) {
        this.stock = stock.decrease(quantity);
    }

    // =============================
    // 🔒 Validation methods
    // =============================

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
    }

    private void validateBrandId(Long brandId) {
        if (brandId == null || brandId <= 0) {
            throw new IllegalArgumentException("브랜드 ID는 필수입니다.");
        }
    }
}
