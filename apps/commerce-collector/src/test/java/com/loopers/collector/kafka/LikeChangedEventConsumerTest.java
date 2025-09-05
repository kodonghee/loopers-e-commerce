package com.loopers.collector.kafka;

import com.loopers.collector.entity.EventHandled;
import com.loopers.collector.entity.ProductMetrics;
import com.loopers.collector.repository.EventHandledRepository;
import com.loopers.collector.repository.ProductMetricsRepository;
import com.loopers.events.like.LikeChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
class LikeChangedEventConsumerTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ProductMetricsRepository productMetricsRepository;

    @Autowired
    private EventHandledRepository eventHandledRepository;

    private final String topic = "catalog-events";

    @Test
    void shouldConsumeDuplicateEvents_onlyOnceApplied() throws Exception {
        // given
        Long productId = 100L;
        String userId = "user-123";
        String eventId = UUID.randomUUID().toString();

        LikeChangedEvent event = new LikeChangedEvent(
                eventId,
                Instant.now(),
                productId,
                userId,
                true // liked
        );

        // when - 같은 이벤트를 여러 번 전송
        for (int i = 0; i < 3; i++) {
            kafkaTemplate.send(topic, productId.toString(), event);
        }

        // then - 처리될 시간을 조금 기다림
        Thread.sleep(2000);

        ProductMetrics metrics = productMetricsRepository
                .findByProductIdAndDate(productId, LocalDate.now())
                .orElseThrow();

        EventHandled handled = eventHandledRepository.findById(eventId)
                .orElseThrow();

        // 멱등 처리
        assertThat(metrics.getLikeCount()).isEqualTo(1);
        assertThat(handled.getEventId()).isEqualTo(eventId);
    }
}
