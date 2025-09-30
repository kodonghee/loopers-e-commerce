package com.loopers.events.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(
        String eventId,
        Instant occurredAt,
        String orderId,
        String userId,
        BigDecimal totalAmount,
        List<OrderItemDto> items
) {
    public record OrderItemDto(Long productId, int quantity, BigDecimal price) {}

    @JsonCreator
    public OrderPlacedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("userId") String userId,
            @JsonProperty("totalAmount") BigDecimal totalAmount,
            @JsonProperty("items") List<OrderItemDto> items
    ) {
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public static OrderPlacedEvent of(String orderId, String userId, BigDecimal totalAmount, List<OrderItemDto> items) {
        return new OrderPlacedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                orderId,
                userId,
                totalAmount,
                items);
    }
}
