package com.loopers.interfaces.api.ranking;

import com.loopers.application.product.ProductResult;
import com.loopers.domain.product.ProductSearchCondition;

import java.math.BigDecimal;

public class RankingV1Dto {
    public record RankingResponse(
            Long productId,
            String name,
            Integer stock,
            BigDecimal price,
            String brandName,
            Long likeCount,
            Long rank
    ) {
        public static RankingResponse from(ProductResult info) {
            return new RankingResponse(
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

}
