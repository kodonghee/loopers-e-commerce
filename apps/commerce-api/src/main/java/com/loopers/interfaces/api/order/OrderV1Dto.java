package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderInfo;

import java.util.List;

public class OrderV1Dto {

    public record OrderResponse(
            Long orderId,
            String userId,
            int totalAmount,
            List<OrderItemResponse> items
    ) {
        public static OrderResponse from(OrderInfo info) {
            return new OrderResponse(
                    info.orderId(),
                    info.userId(),
                    info.totalAmount(),
                    info.items().stream().map(OrderItemResponse::from).toList()
            );
        }
    }

    public record OrderItemResponse(Long productId, int quantity, int price) {
        public static OrderItemResponse from(OrderInfo.OrderItemInfo info) {
            return new OrderItemResponse(info.productId(), info.quantity(), info.price());
        }
    }

    public class OrderItemRequestList {
        private List<OrderItemRequest> items;

        public List<OrderItemRequest> getItems() {
            return items;
        }

        public record OrderItemRequest(Long productId, int quantity, int price) {}
    }
}
