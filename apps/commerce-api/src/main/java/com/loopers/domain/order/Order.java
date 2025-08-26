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

    private String orderId;

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
        this.orderId = UUID.randomUUID().toString();
    }

    public static Order createPending(String userId, List<OrderItem> items, PaymentMethod paymentMethod) {
        return new Order(userId, items, paymentMethod);
    }

    public String getOrderId() { return orderId; }

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

    public void paid() {
        if (this.status != OrderStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException("결제 대기 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.PAID;
    }

    public void declinePayment() {
        if (this.status != OrderStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException("결제 대기 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.PAYMENT_DECLINED;
    }

    public void errorPayment() {
        if (this.status != OrderStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException("결제 대기 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.PAYMENT_ERROR;
    }

    public void cancel() {
        if (this.status == OrderStatus.PAID) {
            throw new IllegalStateException("결제 완료된 주문은 취소할 수 없습니다.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public OrderStatus getStatus() { return status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }

    public boolean isPaid() { return this.status == OrderStatus.PAID; }
    public boolean isAwaitingPayment() { return this.status == OrderStatus.AWAITING_PAYMENT; }
    public boolean isDeclined() { return this.status == OrderStatus.PAYMENT_DECLINED; }
    public boolean isError() { return this.status == OrderStatus.PAYMENT_ERROR; }
    public boolean isCancelled() { return this.status == OrderStatus.CANCELLED; }
}
