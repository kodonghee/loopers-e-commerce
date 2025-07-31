package com.loopers.application.order;

import java.util.List;

public record OrderCommand(
        String userId,
        List<OrderItem> items
) {
    public record OrderItem(Long productId, int quantity, int price) {}
}
