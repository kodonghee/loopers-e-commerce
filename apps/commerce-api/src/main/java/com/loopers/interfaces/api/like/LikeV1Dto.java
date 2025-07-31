package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeInfo;

public class LikeV1Dto {

    public record LikeProductResponse(Long productId) {
        public static LikeProductResponse from(LikeInfo info) {
            return new LikeProductResponse(info.productId());
        }
    }
}
