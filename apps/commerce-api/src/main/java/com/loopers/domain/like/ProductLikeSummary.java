package com.loopers.domain.like;

import com.loopers.domain.product.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "product_like_summary")
public class ProductLikeSummary {

    @Id
    private Long productId;

    @Column(nullable = false)
    private Long likeCount;

    /*@Version
    private Long version;*/

    protected ProductLikeSummary() {}

    public ProductLikeSummary(Long productId) {
        this.productId = productId;
        this.likeCount = 0L;
    }

    public void increment() {
        if (this.likeCount == null) this.likeCount = 0L;
        this.likeCount++;
    }

    public void decrement() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public Long getProductId() {
        return productId;
    }

    public Long getLikeCount() {
        return likeCount;
    }
}
