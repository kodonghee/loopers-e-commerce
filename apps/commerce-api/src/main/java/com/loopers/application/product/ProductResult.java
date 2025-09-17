package com.loopers.application.product;

import java.math.BigDecimal;

public record ProductResult(
        Long productId,
        String name,
        int stock,
        BigDecimal price,
        String brandName,
        Long likeCount,
        Long rank
) {

    public ProductResult(Long productId, String name, int stock, BigDecimal price, String brandName, Long likeCount) {
        this(productId, name, stock, price, brandName, likeCount, null);
    }

    public ProductResult withRank(Long rank) {
        return new ProductResult(productId, name, stock, price, brandName, likeCount, rank);
    }
}

