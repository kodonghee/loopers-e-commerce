package com.loopers.collector.ranking;

import java.math.BigDecimal;

public class RankingScore {

    private static final double LIKE_WEIGHT = 1.0;
    private static final double ORDER_COUNT_WEIGHT = 2.0;
    private static final double ORDER_QUANTITY_WEIGHT = 0.5;
    private static final double VIEW_WEIGHT = 0.2;

    public static double fromLike() {
        return LIKE_WEIGHT;
    }

    public static double fromOrder(int quantity) {
        return ORDER_COUNT_WEIGHT + ORDER_QUANTITY_WEIGHT * quantity;
    }

    public static double fromView() {
        return VIEW_WEIGHT;
    }
}
