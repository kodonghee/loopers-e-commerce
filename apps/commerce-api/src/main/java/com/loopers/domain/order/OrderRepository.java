package com.loopers.domain.order;

import com.loopers.domain.user.UserId;
import feign.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    List<Order> findAllByUserId(UserId userId);
    Optional<Order> findById(Long id);
    Optional<Order> findByOrderId(String orderId);
    int markOrderFailed(String orderId);
}
