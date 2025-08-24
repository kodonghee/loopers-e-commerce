package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderResult;

import java.math.BigDecimal;
import java.util.List;

public class OrderV1Dto {

    public record OrderResponse(
            String orderId,
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
        private String paymentMethod;

        public List<OrderItemRequest> getItems() {
            return items;
        }
        public Long getCouponId() { return couponId; }
        public String getPaymentMethod() {return paymentMethod; }
        public record OrderItemRequest(Long productId, int quantity, BigDecimal price) {}

    }
}
