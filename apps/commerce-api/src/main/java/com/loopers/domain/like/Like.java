package com.loopers.domain.like;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_like", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    protected Like() {}

    public Like(String userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }
}
