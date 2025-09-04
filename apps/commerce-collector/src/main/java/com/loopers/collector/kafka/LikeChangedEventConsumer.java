package com.loopers.collector.kafka;

import com.loopers.events.like.LikeChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LikeChangedEventConsumer {

    @KafkaListener(
            topics = "catalog-events",
            groupId = "catalog-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(LikeChangedEvent event, Acknowledgment ack) {
        try {
            log.info("Consumed LikeChangedEvent: {}", event);

            // TODO: 이후 DB 집계나 감사 로그 저장 처리
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process LikeChangedEvent: {}", event, e);
        }
    }
}
