package com.loopers.collector.ranking;

import java.math.BigDecimal;

public class RankingScore {

    private static final double LIKE_WEIGHT = 0.3;
    private static final double ORDER_WEIGHT = 0.7;

    public static double fromLike() {
        return LIKE_WEIGHT * 1;
    }

    public static double fromOrder(BigDecimal price, int amount) {
        return ORDER_WEIGHT * price.multiply(BigDecimal.valueOf(amount)).doubleValue();
    }
}
