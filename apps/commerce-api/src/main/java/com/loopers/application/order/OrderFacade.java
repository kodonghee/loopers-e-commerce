package com.loopers.application.order;

import com.loopers.application.order.port.OrderEventSender;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.loopers.application.order.OrderMapper.fromOrder;
import static com.loopers.application.order.OrderMapper.toOrder;

@RequiredArgsConstructor
@Service
public class OrderFacade {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PointRepository pointRepository;
    private final OrderEventSender orderEventSender;

    @Transactional
    public OrderResult placeOrder(OrderCriteria criteria) {

        Order order = toOrder(criteria);

        // 1. 상품 재고 확인 및 차감
        for (OrderItem item : order.getOrderItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST));

            product.decreaseStock(item.getQuantity()); // 재고 유효성 검사 포함
        }

        // 2. 포인트 확인 및 차감
        Point point = pointRepository.find(new UserId(order.getUserId()))
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST));
        point.use(order.getTotalAmount()); // 포인트 유효성 검사 포함

        // 3. 주문 정보 외부 시스템 전송
        orderEventSender.send(order.getOrderId());

        // 4. 주문 저장
        orderRepository.save(order);

        return fromOrder(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResult> getOrderList(UserId userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(this::toInfo)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResult getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        return toInfo(order);
    }

    private OrderResult toInfo(Order o) { // Mapper에 존재하므로 생략해도 됨
        return new OrderResult(
                o.getId(),
                o.getUserId(),
                o.getTotalAmount(),
                o.getOrderItems().stream()
                        .map(i -> new OrderResult.OrderItemResult(i.getProductId(), i.getQuantity(), i.getPrice()))
                        .collect(Collectors.toList())
        );
    }
}
