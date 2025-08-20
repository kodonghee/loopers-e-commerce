package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResult createPendingOrder(OrderCriteria criteria) {

        Order order = Order.createPending(
                criteria.userId(),
                criteria.paymentMethod(),
                criteria.items().stream()
                        .map(i -> new OrderItem(i.productId(), i.quantity(), i.price()))
                        .toList()
        );
        orderRepository.save(order);
        return OrderMapper.fromOrder(order);
    }

    @Transactional
    public void markOrderPaid(Long orderId) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."))
                .markPaid();
    }

    @Transactional
    public void markOrderFailed(Long orderId) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."))
                .markPaymentFailed();
    }

    @Transactional(readOnly = true)
    public List<OrderResult> getOrderList(UserId userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(OrderMapper::fromOrder)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResult getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        return OrderMapper.fromOrder(order);
    }
}
