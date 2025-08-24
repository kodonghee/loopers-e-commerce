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
    private Status status = Status.PENDING;
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;
    @Column(length = 255)
    private String reason;

    protected Payment(){};

    private Payment(String orderId, String userId, BigDecimal amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = Status.PENDING;
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
    public Status getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public String getReason() { return reason; }

    public void startNewAttempt() {
        if (this.status == Status.SUCCESS) {
            throw new IllegalStateException("이미 지불 되었습니다.");
        }
        this.status = Status.PENDING;
        this.externalPaymentId = null;
        this.reason = null;
        this.attemptCount += 1;
    }

    public boolean isFinalized() { return status != Status.PENDING; }

    public void markRequested(String externalId) {
        if (this.status != Status.PENDING) return;
        if (this.externalPaymentId == null) {
            this.externalPaymentId = externalId;
        }
    }

    public void applyCallback(String extId, Status newStatus, String reason) {
        if (this.status != Status.PENDING) return;
        if (this.externalPaymentId != null && !this.externalPaymentId.equals(extId)) return;
        this.status = newStatus;
        this.reason = reason;
    }

    @Deprecated
    public void applyResult(String status, String reason) {
        applyCallback(this.externalPaymentId, Status.valueOf(status), reason);
    }

    public boolean isSuccess() {
        return this.status == Status.SUCCESS;
    }

    public boolean isFailed() {
        return this.status == Status.FAILED
                || this.status == Status.INVALID_CARD
                || this.status == Status.LIMIT_EXCEEDED;
    }

    public enum Status { PENDING, SUCCESS, LIMIT_EXCEEDED, INVALID_CARD, FAILED }
}

