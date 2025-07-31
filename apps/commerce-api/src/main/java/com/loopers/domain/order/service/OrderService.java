package com.loopers.domain.order.service;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.List;
import java.util.function.BiFunction;

public class OrderService {

    private final BiFunction<Long, Integer, Boolean> stockValidator;
    private final BiFunction<String, Integer, Boolean> pointValidator;

    public OrderService(BiFunction<Long, Integer, Boolean> stockValidator,
                        BiFunction<String, Integer, Boolean> pointValidator) {
        this.stockValidator = stockValidator;
        this.pointValidator = pointValidator;
    }

    public Order createOrder(String userId, List<OrderItem> items) {
        int totalAmount = items.stream().mapToInt(OrderItem::getTotalPrice).sum();

        for (OrderItem item : items) {
            if (!stockValidator.apply(item.getProductId(), item.getQuantity())) {
                throw new CoreException(ErrorType.BAD_REQUEST);
            }
        }

        if (!pointValidator.apply(userId, totalAmount)) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }

        return new Order(userId, items);
    }
}
