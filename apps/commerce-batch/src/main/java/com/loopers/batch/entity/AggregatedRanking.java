package com.loopers.batch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AggregatedRanking {
    private Long productId;
    private long likeCount;
    private long orderCount;
    private long orderQuantity;
    private long viewCount;
}
