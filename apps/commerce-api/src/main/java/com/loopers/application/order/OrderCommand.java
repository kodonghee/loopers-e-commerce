package com.loopers.application.order;

import java.util.List;

public record OrderCommand(
        String userId,
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(Long productId, int quantity, int price) {}
}
