package com.loopers.application.like;

import com.loopers.domain.like.Like;

public class LikeMapper {

    public static Like toLike(LikeCriteria criteria) {
        return new Like(criteria.userId(), criteria.productId());
    }

    public static LikeResult fromLike(Like like) {
        return new LikeResult(like.getUserId(), like.getProductId());
    }
}
