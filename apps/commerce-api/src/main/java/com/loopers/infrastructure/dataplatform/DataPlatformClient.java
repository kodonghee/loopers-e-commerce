package com.loopers.infrastructure.dataplatform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataPlatformClient {

    public void sendOrderData(Object payload) {
        // TODO: 실제 연동 시 REST/Kafka 전송 로직
        log.info("[DataPlatform] 주문 데이터 전송: {}", payload);
    }

    public void sendPaymentData(Object payload) {
        log.info("[DataPlatform] 결제 데이터 전송: {}", payload);
    }
}
