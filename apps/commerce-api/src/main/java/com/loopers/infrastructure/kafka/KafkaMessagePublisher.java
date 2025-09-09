package com.loopers.infrastructure.kafka;

import com.loopers.application.event.MessagePublisher;
import com.loopers.events.like.LikeChangedEvent;
import com.loopers.events.stock.StockAdjustedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessagePublisher implements MessagePublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Override
    public void publish(Object event) {
        String topic = resolveTopic(event);
        Object key = resolveKey(event);

        log.info("Sending event={} to topic={} with key={}", event, topic, key);
        kafkaTemplate.send(topic, key, event);
    }

    private String resolveTopic(Object event) {
        if (event instanceof LikeChangedEvent || event instanceof StockAdjustedEvent) {
            return "catalog-events";
        }

        // TODO: 다른 이벤트 매핑 필요 시 추가
        throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
    }

    private Object resolveKey(Object event) {
        if (event instanceof LikeChangedEvent likeEvent) {
            return String.valueOf(likeEvent.productId());
        }
        if (event instanceof StockAdjustedEvent stockEvent) {
            return String.valueOf(stockEvent.productId());
        }
        return null;
    }
}
