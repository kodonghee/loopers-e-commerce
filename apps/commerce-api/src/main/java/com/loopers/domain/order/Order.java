package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status = OrderStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentMethod paymentMethod = PaymentMethod.POINTS;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();

    protected Order() {}

    public Order(String userId, List<OrderItem> items) {
        this.userId = userId;
        this.orderItems.addAll(items);
    }

    public Long getOrderId() { return id; }

    public String getUserId() { return userId; }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public BigDecimal getTotalAmount() {
        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markAwaitingPayment() { this.status = OrderStatus.AWAITING_PAYMENT; }
    public void markPaid()            { this.status = OrderStatus.PAID; }
    public void markPaymentFailed()   { this.status = OrderStatus.PAYMENT_FAILED; }

    public OrderStatus getStatus() { return status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
}
