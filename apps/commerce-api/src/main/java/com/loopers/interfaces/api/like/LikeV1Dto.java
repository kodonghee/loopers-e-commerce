package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeResult;

public class LikeV1Dto {

    public record LikeProductResponse(Long productId) {
        public static LikeProductResponse from(LikeResult info) {
            return new LikeProductResponse(info.productId());
        }
    }

    public record LikeActionResponse(boolean changed) {}
}
