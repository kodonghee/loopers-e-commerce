package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name="payment", uniqueConstraints = {
        @UniqueConstraint(name="uk_payment_external", columnNames="external_payment_id")
})
public class Payment extends BaseEntity {
    @Column(name="order_id", nullable=false, updatable = false)
    private String orderId;
    @Column(name="user_id",  nullable=false, updatable = false)
    private String userId;
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    @Column(name="external_payment_id",  length = 64)
    private String externalPaymentId;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length = 32)
    private PaymentStatus status = PaymentStatus.PENDING;
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;
    @Column(length = 255)
    private String reason;

    protected Payment(){};

    private Payment(String orderId, String userId, BigDecimal amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.attemptCount = 0;
        this.externalPaymentId = null;
        this.reason = null;
    }
    public static Payment newForOrder(String orderId, String userId, BigDecimal amount) {
        return new Payment(orderId, userId, amount);
    }

    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public String getExternalPaymentId() { return externalPaymentId; }
    public PaymentStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public String getReason() { return reason; }

    public void startNewAttempt() {
        if (this.status == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("이미 지불 되었습니다.");
        }
        this.status = PaymentStatus.PENDING;
        this.externalPaymentId = null;
        this.reason = null;
        this.attemptCount += 1;
    }

    public boolean isFinalized() { return this.status.isFinalized(); }

    public void markRequested(String externalId) {
        if (this.status != PaymentStatus.PENDING) return;
        if (this.externalPaymentId == null) {
            this.externalPaymentId = externalId;
        }
    }

    public void applyCallback(String extId, PaymentStatus newStatus, String reason) {
        if (this.status != PaymentStatus.PENDING) return;
        if (this.externalPaymentId != null && !this.externalPaymentId.equals(extId)) return;
        this.status = newStatus;
        this.reason = reason;
    }

    @Deprecated
    public void applyResult(String status, String reason) {
        applyCallback(this.externalPaymentId, PaymentStatus.valueOf(status), reason);
    }

    public boolean isSuccess() {
        return this.status.isSuccess();
    }

    public boolean isFailed() {
        return this.status.isFailure();
    }
}

