package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "product_like", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class Like extends BaseEntity {

    @Version
    private Long version;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    protected Like() {}

    public Like(String userId, Long productId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수 입력 값 입니다.");
        }

        if (productId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수 입력 값 입니다.");
        }
        this.userId = userId;
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Like like = (Like) o;
        return Objects.equals(userId, like.userId) && Objects.equals(productId, like.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, productId);
    }
}
