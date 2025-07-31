package com.loopers.infrastructure.order;

import com.loopers.application.order.port.OrderEventSender;
import org.springframework.stereotype.Component;

@Component
public class DummyOrderEventSender implements OrderEventSender {
    @Override
    public void send(Long orderId) {

    }
}
