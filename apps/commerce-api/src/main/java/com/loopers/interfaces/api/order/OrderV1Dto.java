package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderResult;

import java.math.BigDecimal;
import java.util.List;

public class OrderV1Dto {

    public record OrderResponse(
            Long orderId,
            String userId,
            BigDecimal totalAmount,
            List<OrderItemResponse> items
    ) {
        public static OrderResponse from(OrderResult info) {
            return new OrderResponse(
                    info.orderId(),
                    info.userId(),
                    info.totalAmount(),
                    info.items().stream().map(OrderItemResponse::from).toList()
            );
        }
    }

    public record OrderItemResponse(Long productId, int quantity, BigDecimal price) {
        public static OrderItemResponse from(OrderResult.OrderItemResult info) {
            return new OrderItemResponse(info.productId(), info.quantity(), info.price());
        }
    }

    public class PlaceOrderRequest {
        private List<OrderItemRequest> items;
        private Long couponId;

        public List<OrderItemRequest> getItems() {
            return items;
        }

        public Long getCouponId() { return couponId; }

        public record OrderItemRequest(Long productId, int quantity, BigDecimal price) {}

    }
}
