package com.loopers.application.order;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

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
        return OrderMapper.fromOrder(order);
    }

    @Transactional
    public BigDecimal prepareForPayment(String orderId, Long couponId) {
        Order order = getOrderEntity(orderId);

        BigDecimal finalAmount = order.getTotalAmount();

        if (couponId != null) {
            Coupon coupon = couponRepository.findByIdForUpdate(couponId)
                    .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 쿠폰입니다."));
            coupon.checkOwner(order.getUserId());
            BigDecimal discount = coupon.calculateDiscount(order.getTotalAmount());
            order.applyDiscount(discount);
            finalAmount = order.getFinalAmount();
        }

        return finalAmount;
    }

    @Transactional
    public void confirmPayment(String orderId, Long couponId) {
        Order order = getOrderEntity(orderId);

        // 재고 차감
        List<Long> productIds = order.getOrderItems().stream()
                .map(OrderItem::getProductId)
                .toList();

        List<Product> products = productRepository.findByIdForUpdate(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        order.getOrderItems().forEach(item -> {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("상품을 찾을 수 없습니다. id=" + item.getProductId());
            }
            product.decreaseStock(item.getQuantity());
        });

        // 쿠폰 적용
        if (couponId != null) {
            Coupon coupon = couponRepository.findByIdForUpdate(couponId).orElseThrow();
            coupon.markAsUsed();
        }

        // 주문 상태 변경
        order.paid();
    }

    @Transactional
    public void markOrderFailed(String orderId, boolean businessFailure) {
        Order order = getOrderEntity(orderId);
        if (businessFailure) {
            order.declinePayment();
        } else {
            order.errorPayment();
        }
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
}
