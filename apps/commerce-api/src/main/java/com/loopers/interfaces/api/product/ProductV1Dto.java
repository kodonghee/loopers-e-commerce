package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductResult;
import com.loopers.domain.product.ProductSearchCondition;

import java.math.BigDecimal;

public class ProductV1Dto {
    public record ProductListResponse(
            Long productId,
            String name,
            Integer stock,
            BigDecimal price,
            String brandName,
            Long likeCount
    ) {
        public static ProductListResponse from(ProductResult info) {
            return new ProductListResponse(
                    info.productId(),
                    info.name(),
                    info.stock(),
                    info.price(),
                    info.brandName(),
                    info.likeCount()
            );
        }
    }

    public record ProductDetailResponse(
            Long productId,
            String name,
            Integer stock,
            BigDecimal price,
            String brandName,
            Long likeCount,
            Long rank
    ) {
        public static ProductDetailResponse from(ProductResult info) {
            return new ProductDetailResponse(
                    info.productId(),
                    info.name(),
                    info.stock(),
                    info.price(),
                    info.brandName(),
                    info.likeCount(),
                    info.rank()
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
