package com.loopers.collector.kafka;

import com.loopers.collector.entity.EventHandled;
import com.loopers.collector.repository.EventHandledRepository;
import com.loopers.collector.service.ProductMetricsService;
import com.loopers.events.order.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPlacedMetricsConsumer {

    private final ProductMetricsService productMetricsService;
    private final EventHandledRepository eventHandledRepository;

    private static final String CONSUMER_NAME = "metrics";

    @KafkaListener(
            topics = "order-events",
            groupId = "order-metrics-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(OrderPlacedEvent event, Acknowledgment ack) {
        try {
            String handledId = event.eventId() + ":" + CONSUMER_NAME;
            if (eventHandledRepository.findById(handledId).isPresent()) {
                log.debug("Duplicate event detected for metrics. Skipping. eventId={}", event.eventId());
                ack.acknowledge();
                return;
            }

            LocalDate today = LocalDate.now();
            event.items().forEach(item ->
                    productMetricsService.handleOrder(item.productId(), today, item.quantity())
            );

            eventHandledRepository.saveAndFlush(new EventHandled(event.eventId(), CONSUMER_NAME));

            log.info("Metrics updated for order event. orderId={}, date={}", event.orderId(), today);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process OrderPlacedEvent for metrics: {}", event, e);
        }
    }
}
