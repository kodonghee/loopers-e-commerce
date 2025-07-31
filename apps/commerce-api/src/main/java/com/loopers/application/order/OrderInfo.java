package com.loopers.application.order;

import java.util.List;

public record OrderInfo(
        Long orderId,
        String userId,
        int totalAmount,
        List<OrderItemInfo> items
) {
    public record OrderItemInfo(Long productId, int quantity, int price) {}
}
