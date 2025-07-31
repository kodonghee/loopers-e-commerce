package com.loopers.application.order;

import com.loopers.application.order.port.OrderEventSender;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final OrderEventSender orderEventSender;

    @Transactional
    public Long placeOrder(OrderCommand command) {
        List<OrderItem> items = command.items().stream()
                .map(i -> new OrderItem(i.productId(), i.quantity(), i.price()))
                .toList();

        Order order = orderService.createOrder(command.userId(), items);
        orderRepository.save(order);

        orderEventSender.sendOrderEvent(order.getId());

        return order.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderInfo> getOrderList(String userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(this::toInfo)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderInfo getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        return toInfo(order);
    }

    private OrderInfo toInfo(Order o) {
        return new OrderInfo(
                o.getId(),
                o.getUserId(),
                o.getTotalAmount(),
                o.getOrderItems().stream()
                        .map(i -> new OrderInfo.OrderItemInfo(i.getProductId(), i.getQuantity(), i.getTotalPrice() / i.getQuantity()))
                        .toList()
        );
    }
}
