package com.loopers.collector.kafka;

import com.loopers.collector.entity.EventHandled;
import com.loopers.collector.repository.EventHandledRepository;
import com.loopers.config.redis.RedisCacheConfig;
import com.loopers.events.stock.StockAdjustedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockAdjustedCacheEvictConsumer {

    private final EventHandledRepository eventHandledRepository;
    private final CacheManager cacheManager;

    private static final String CONSUMER_NAME = "cache-evict";

    @KafkaListener(
            topics = "catalog-events",
            groupId = "catalog-cache-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(StockAdjustedEvent event, Acknowledgment ack) {
        try {
            String handledId = event.eventId() + ":" + CONSUMER_NAME;
            if (eventHandledRepository.findById(handledId).isPresent()) {
                log.info("Duplicate StockAdjustedEvent. Skipping. eventId={}", event.eventId());
                ack.acknowledge();
                return;
            }

            if (event.newStock() == 0) {
                Cache cache = cacheManager.getCache(RedisCacheConfig.CACHE_PRODUCT_DETAIL);
                cache.evict(event.productId());
                log.info("Cache evicted for productId={} (stock=0)", event.productId());
            }

            eventHandledRepository.saveAndFlush(new EventHandled(event.eventId(), CONSUMER_NAME));
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process StockAdjustedEvent: {}", event, e);
        }
    }
}
