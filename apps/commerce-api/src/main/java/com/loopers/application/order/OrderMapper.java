package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;

import java.util.List;

public class OrderMapper {

    public static Order toOrder(OrderCriteria criteria) {
        List<OrderItem> items = criteria.items().stream()
                .map(item -> new OrderItem(item.productId(), item.quantity(), item.price()))
                .toList();
        return new Order(criteria.userId(), items);
    }

    public static OrderResult fromOrder(Order order) {
        List<OrderResult.OrderItemResult> items = order.getOrderItems().stream()
                .map(item -> new OrderResult.OrderItemResult(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();
        return new OrderResult(order.getOrderId(), order.getUserId(), order.getTotalAmount(), items);
    }
}
