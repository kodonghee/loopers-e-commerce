package com.loopers.application.order.port;

public interface OrderEventSender {
    void sendOrderEvent(Long orderId);
}
