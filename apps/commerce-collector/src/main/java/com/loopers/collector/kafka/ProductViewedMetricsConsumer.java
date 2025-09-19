package com.loopers.collector.kafka;

import com.loopers.collector.entity.EventHandled;
import com.loopers.collector.repository.EventHandledRepository;
import com.loopers.collector.service.ProductMetricsService;
import com.loopers.events.view.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductViewedMetricsConsumer {

    private final ProductMetricsService productMetricsService;
    private final EventHandledRepository eventHandledRepository;

    private static final String CONSUMER_NAME = "metrics";

    @KafkaListener(
            topics = "view-events",
            groupId = "view-metrics-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(ProductViewedEvent event, Acknowledgment ack) {
        try {
            String handledId = event.eventId() + ":" + CONSUMER_NAME;
            if (eventHandledRepository.findById(handledId).isPresent()) {
                log.debug("Duplicate event detected for metrics. Skipping. eventId={}", event.eventId());
                ack.acknowledge();
                return;
            }

            productMetricsService.handleView(event.productId(), LocalDate.now());
            eventHandledRepository.save(new EventHandled(event.eventId(), CONSUMER_NAME));

            log.info("Metrics updated for product view. productId={}, date={}", event.productId(), LocalDate.now());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process ProductViewedEvent for metrics: {}", event, e);
        }
    }
}
