package com.loopers.domain.like;

import jakarta.persistence.*;

@Entity
@Table(name = "product_like_summary")
public class ProductLikeSummary {

    @Id
    private Long productId;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(nullable = false)
    private Long likeCount;

    protected ProductLikeSummary() {}

    public ProductLikeSummary(Long productId, Long brandId) {
        this.productId = productId;
        this.brandId = brandId;
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

    public Long getBrandId() {
        return brandId;
    }

    public Long getLikeCount() {
        return likeCount;
    }
}
