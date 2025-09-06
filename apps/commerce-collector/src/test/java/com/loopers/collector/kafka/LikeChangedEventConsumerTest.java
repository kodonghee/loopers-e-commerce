package com.loopers.collector.kafka;

import com.loopers.collector.config.TestKafkaConfig;
import com.loopers.collector.entity.EventLog;
import com.loopers.collector.entity.ProductMetrics;
import com.loopers.collector.repository.EventHandledRepository;
import com.loopers.collector.repository.EventLogRepository;
import com.loopers.collector.repository.ProductMetricsRepository;
import com.loopers.events.like.LikeChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
        properties = {
                "spring.kafka.consumer.group-id=test-consumer",
                "spring.kafka.listener.concurrency=1"
        }
)
@DirtiesContext // 테스트마다 context 초기화
@EmbeddedKafka(
        partitions = 1,
        topics = {"catalog-events"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:19092", "port=19092"}
)
@Import(TestKafkaConfig.class)
class LikeChangedEventConsumerTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EventHandledRepository eventHandledRepository;

    @Autowired
    private EventLogRepository eventLogRepository;

    @Autowired
    private ProductMetricsRepository productMetricsRepository;

    private static final String CONSUMER_NAME = "log"; // LikeChangedEventConsumer 의 consumerName

    @BeforeEach
    void setUp() {

        eventHandledRepository.deleteAll();
        eventLogRepository.deleteAll();
        productMetricsRepository.deleteAll();
    }

    @Test
    void shouldConsumeDuplicateEvents_onlyOnceApplied_andAffectMetrics() throws Exception {
        String eventId = UUID.randomUUID().toString();
        String handledId = eventId + ":" + CONSUMER_NAME;

        LikeChangedEvent event = new LikeChangedEvent(
                eventId,
                Instant.now(),
                1001L,
                "user-123",
                true
        );

        // 동일 이벤트 두 번 발행
        kafkaTemplate.send("catalog-events", String.valueOf(event.productId()), event).get(5, TimeUnit.SECONDS);
        kafkaTemplate.send("catalog-events", String.valueOf(event.productId()), event).get(5, TimeUnit.SECONDS);

        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    assertThat(eventHandledRepository.findById(handledId)).isPresent();
                    assertThat(eventHandledRepository.count()).isEqualTo(2);

                    EventLog logEntry = eventLogRepository.findByEventId(eventId)
                            .orElseThrow();
                    assertThat(logEntry.getEventType()).isEqualTo("LikeChangedEvent");

                    ProductMetrics metrics = productMetricsRepository
                            .findByProductIdAndDate(event.productId(), LocalDate.now())
                            .orElseThrow();
                    assertThat(metrics.getLikeCount()).isEqualTo(1);
                });
    }
}
