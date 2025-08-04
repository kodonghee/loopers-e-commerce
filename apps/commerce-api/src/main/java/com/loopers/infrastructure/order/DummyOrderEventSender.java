package com.loopers.infrastructure.order;

import com.loopers.application.order.port.OrderEventSender;
import org.springframework.stereotype.Component;

@Component
public class DummyOrderEventSender implements OrderEventSender {
    @Override
    public void send(Long orderId) {
        System.out.println("Dummy event sent for order: " + orderId);
    }
}
