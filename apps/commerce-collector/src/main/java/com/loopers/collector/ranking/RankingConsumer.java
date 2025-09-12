package com.loopers.collector.ranking;

import com.loopers.events.like.LikeChangedEvent;
import com.loopers.events.order.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingConsumer {

    private final RankingWriter rankingWriter;

    @KafkaListener(topics = "like-events", groupId = "ranking-consumer")
    public void consumeLikeChanged(LikeChangedEvent event) {
        log.info("Consume LikeChangedEvent: {}", event);

        double score = RankingScore.fromLike();
        rankingWriter.incrementScore(
                LocalDate.now(),
                event.productId().toString(),
                score
        );
    }

    @KafkaListener(topics = "order-events", groupId = "ranking-consumer")
    public void consumeOrderPlaced(OrderPlacedEvent event) {
        log.info("Consume OrderPlacedEvent: {}", event);

        event.items().forEach(item -> {
            double score = RankingScore.fromOrder(item.price(), item.quantity());
            rankingWriter.incrementScore(
                    LocalDate.now(),
                    item.productId().toString(),
                    score
            );
        });
    }
}
