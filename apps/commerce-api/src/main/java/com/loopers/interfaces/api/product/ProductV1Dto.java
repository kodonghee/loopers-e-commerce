package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;

import java.math.BigDecimal;

public class ProductV1Dto {
    public record ProductResponse(
            Long productId,
            String name,
            Integer stock,
            BigDecimal price,
            String brandName,
            Integer likeCount
    ) {
        public static ProductResponse from(ProductInfo info) {
            return new ProductResponse(
                    info.productId(),
                    info.name(),
                    info.stock(),
                    info.price(),
                    info.brandName(),
                    info.likeCount()
            );
        }
    }

}
