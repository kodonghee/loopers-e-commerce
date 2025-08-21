package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    private String pgOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status = OrderStatus.AWAITING_PAYMENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentMethod paymentMethod = PaymentMethod.POINTS;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "final_amount", nullable = false)
    private BigDecimal finalAmount;

    protected Order() {}

    private Order(String userId, List<OrderItem> items, PaymentMethod paymentMethod) {
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.orderItems.addAll(items);
        this.status = OrderStatus.AWAITING_PAYMENT;
        this.finalAmount = getTotalAmount();
        this.pgOrderId = String.valueOf(System.currentTimeMillis());
    }

    public static Order createPending(String userId, List<OrderItem> items, PaymentMethod paymentMethod) {
        return new Order(userId, items, paymentMethod);
    }

    public Long getOrderId() { return id; }

    public String getPgOrderId() {return pgOrderId; }

    public String getUserId() { return userId; }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public BigDecimal getTotalAmount() {
        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void applyDiscount(BigDecimal discountAmount) {
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("할인 금액은 음수가 될 수 없습니다.");
        }

        if (discountAmount.compareTo(getTotalAmount()) > 0) {
            throw new IllegalArgumentException("할인 금액은 주문 금액을 초과할 수 없습니다.");
        }
        this.finalAmount = this.getFinalAmount().subtract(discountAmount);
    }

    public BigDecimal getFinalAmount() { return finalAmount; }

    public void markAwaitingPayment() { this.status = OrderStatus.AWAITING_PAYMENT; }
    public void markPaid() { this.status = OrderStatus.PAID; }
    public void markPaymentFailed()   { this.status = OrderStatus.PAYMENT_FAILED; }
    public void markCancelled() {this.status = OrderStatus.CANCELLED; }

    public OrderStatus getStatus() { return status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
}
