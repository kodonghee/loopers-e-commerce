package com.loopers.application.order.port;

public interface OrderEventSender {
    void send(Long orderId);
}
