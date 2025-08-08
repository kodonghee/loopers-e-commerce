package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductResult;
import com.loopers.domain.product.ProductSearchCondition;

import java.math.BigDecimal;

public class ProductV1Dto {
    public record ProductResponse(
            Long productId,
            String name,
            Integer stock,
            BigDecimal price,
            String brandName,
            Long likeCount
    ) {
        public static ProductResponse from(ProductResult info) {
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

    public record ProductListRequest(
            Long brandId,
            ProductSearchCondition.ProductSortType sort,
            Integer page,
            Integer size
    ) {}

}
