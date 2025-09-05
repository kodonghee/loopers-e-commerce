package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    /*주문 생성*/
    @Transactional
    public OrderResult createPendingOrder(OrderCriteria criteria) {
        List<Long> productIds = criteria.items().stream()
                .map(OrderCriteria.OrderLine::productId)
                .toList();

        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        criteria.items().forEach(i -> {
            if (!productMap.containsKey(i.productId())) {
                throw new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 상품입니다.");
            }
        });

        Order order = Order.createPending(
                criteria.userId(),
                criteria.items().stream()
                        .map(i -> new OrderItem(i.productId(), i.quantity(), i.price()))
                        .toList(),
                criteria.paymentMethod()
        );
        orderRepository.save(order);

        eventPublisher.publishEvent(OrderCreatedEvent.of(
                order.getOrderId(),
                order.getUserId(),
                order.getTotalAmount(),
                productIds
        ));
        return OrderMapper.fromOrder(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResult> getOrderList(UserId userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(OrderMapper::fromOrder)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResult getOrderDetail(String orderId) {
        return OrderMapper.fromOrder(getOrderEntity(orderId));
    }

    @Transactional(readOnly = true)
    public Order getOrderEntity(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
    }

    @Transactional
    public Order pay(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        order.paid();
        return orderRepository.save(order);
    }

    @Transactional
    public Order decline(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        order.declinePayment();
        return orderRepository.save(order);
    }

    @Transactional
    public Order error(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        order.errorPayment();
        return orderRepository.save(order);
    }
}
