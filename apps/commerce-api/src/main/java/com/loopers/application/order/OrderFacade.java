package com.loopers.application.order;

import com.loopers.application.order.port.OrderEventSender;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.loopers.application.order.OrderMapper.fromOrder;
import static com.loopers.application.order.OrderMapper.toOrder;

@RequiredArgsConstructor
@Service
public class OrderFacade {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PointRepository pointRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public OrderResult placeOrder(OrderCriteria criteria) {

        Order order = toOrder(criteria);

        // 1. 상품 재고 확인 및 차감
        Map<Long, Integer> orderItems = order.getOrderItems().stream()
                .collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity, Integer::sum));

        List<Product> products = productRepository.findAllById(new ArrayList<>(orderItems.keySet()));
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (Map.Entry<Long, Integer> entry : orderItems.entrySet()) {
            Product product = productMap.get(entry.getKey());
            System.out.println(entry.getKey());
            System.out.println(product);
            if (product == null) {
                throw new CoreException(ErrorType.BAD_REQUEST);
            }
            product.decreaseStock(entry.getValue());
        }

        // 2. 쿠폰 적용 및 차감
        BigDecimal orderTotAmount = order.getTotalAmount();
        if (criteria.couponId() != null) {
            Coupon coupon = couponRepository.findById(criteria.couponId())
                    .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST));

            coupon.checkOwner(order.getUserId());
            orderTotAmount = coupon.applyCoupon(orderTotAmount);
            coupon.markAsUsed();
        }

        // 3. 포인트 확인 및 차감
        Point point = pointRepository.find(new UserId(order.getUserId()))
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST));
        point.use(orderTotAmount); // 포인트 유효성 검사 포함

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
