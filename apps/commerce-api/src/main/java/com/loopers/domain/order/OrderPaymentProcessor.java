package com.loopers.domain.order;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class OrderPaymentProcessor {

    /*
     * 결제 직전 최종 금액 계산
     */
    @Transactional
    public BigDecimal prepareForPayment(Order order, Coupon coupon) {
        if (coupon != null) {
            coupon.checkOwner(order.getUserId());
            BigDecimal discount = coupon.calculateDiscount(order.getTotalAmount());
            order.applyDiscount(discount);
        }
        return order.getFinalAmount();
    }

    /*
     * 결제 확정 처리 (재고 차감 + 쿠폰 사용 + 주문 상태 변경)
     */
    @Transactional
    public void confirmPayment(Order order, List<Product> products, Coupon coupon) {

        // 재고 차감
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        order.getOrderItems().forEach(item -> {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("상품을 찾을 수 없습니다. id=" + item.getProductId());
            }
            product.decreaseStock(item.getQuantity());
        });

        // 쿠폰 사용
        if (coupon != null) {
            coupon.markAsUsed();
        }
    }

}
