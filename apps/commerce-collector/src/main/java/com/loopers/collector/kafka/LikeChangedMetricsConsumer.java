package com.loopers.collector.kafka;

import com.loopers.collector.entity.EventHandled;
import com.loopers.collector.entity.ProductMetrics;
import com.loopers.collector.repository.EventHandledRepository;
import com.loopers.collector.repository.ProductMetricsRepository;
import com.loopers.events.like.LikeChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeChangedMetricsConsumer {

    private final ProductMetricsRepository productMetricsRepository;
    private final EventHandledRepository eventHandledRepository;

    private static final String CONSUMER_NAME = "metrics";

    @KafkaListener(
            topics = "like-events",
            groupId = "like-metrics-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(LikeChangedEvent event, Acknowledgment ack) {
        try {
            String handledId = event.eventId() + ":" + CONSUMER_NAME;
            if (eventHandledRepository.findById(handledId).isPresent()) {
                log.info("Duplicate event detected for metrics. Skipping. eventId={}", event.eventId());
                ack.acknowledge();
                return;
            }

            LocalDate today = LocalDate.now();
            ProductMetrics metrics = productMetricsRepository
                    .findByProductIdAndDate(event.productId(), today)
                    .orElseGet(() -> productMetricsRepository.save(
                            new ProductMetrics(event.productId(), today)
                    ));

            if (event.liked()) {
                metrics.increaseLikes();
            } else {
                metrics.decreaseLikes();
            }

            productMetricsRepository.save(metrics);
            eventHandledRepository.saveAndFlush(new EventHandled(event.eventId(), CONSUMER_NAME));

            log.info("Updated metrics for productId={}, date={}, likeCount={}",
                    event.productId(), today, metrics.getLikeCount());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process LikeChangedEvent for metrics: {}", event, e);
        }
    }
}
