package com.loopers.domain.order;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long productId;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private BigDecimal price;

    protected OrderItem() {}

    public OrderItem(Long productId, int quantity, BigDecimal price) {
        validateQuantity(quantity);
        validatePrice(price);
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getOrderId() { return id; }
    public Long getProductId() {
        return productId;
    }

    public int getQuantity() { return quantity; }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // =============================
    // 🔒 Validation methods
    // =============================

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
    }
}
