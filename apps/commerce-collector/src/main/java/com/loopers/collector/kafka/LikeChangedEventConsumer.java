package com.loopers.collector.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.collector.entity.EventHandled;
import com.loopers.collector.entity.EventLog;
import com.loopers.collector.repository.EventHandledRepository;
import com.loopers.collector.repository.EventLogRepository;
import com.loopers.events.like.LikeChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeChangedEventConsumer {

    private final EventLogRepository eventLogRepository;
    private final EventHandledRepository eventHandledRepository;
    private final ObjectMapper objectMapper;

    private static final String CONSUMER_NAME = "log";

    @KafkaListener(
            topics = "like-events",
            groupId = "like-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(LikeChangedEvent event, Acknowledgment ack) {
        try {
            String handledId = event.eventId() + ":" + CONSUMER_NAME;
            if (eventHandledRepository.findById(handledId).isPresent()) {
                log.info("Duplicate event detected. Skipping. eventId={}", event.eventId());
                ack.acknowledge();
                return;
            }

            String payload = objectMapper.writeValueAsString(event);
            EventLog logEntry = new EventLog(
                    event.eventId(),
                    event.getClass().getSimpleName(),
                    payload,
                    event.occurredAt()
            );
            eventLogRepository.saveAndFlush(logEntry);

            eventHandledRepository.saveAndFlush(new EventHandled(event.eventId(), CONSUMER_NAME));

            log.info("Consumed and processed LikeChangedEvent: {}", event);
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event, e);
            // TODO: DLQ 로직
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process LikeChangedEvent: {}", event, e);
        }
    }
}
