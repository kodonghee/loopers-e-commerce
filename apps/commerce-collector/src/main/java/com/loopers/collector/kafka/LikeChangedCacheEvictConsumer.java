package com.loopers.collector.kafka;

import com.loopers.collector.entity.EventHandled;
import com.loopers.collector.repository.EventHandledRepository;
import com.loopers.config.redis.RedisCacheConfig;
import com.loopers.events.like.LikeChangedEvent;
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
public class LikeChangedCacheEvictConsumer {

    private final EventHandledRepository eventHandledRepository;
    private final CacheManager cacheManager;
    private static final String CONSUMER_NAME = "like-cache-evict";

    @KafkaListener(
            topics = "like-events",
            groupId = "like-cache-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(LikeChangedEvent event, Acknowledgment ack) {
        try {
            String handledId = event.eventId() + ":" + CONSUMER_NAME;
            if (eventHandledRepository.findById(handledId).isPresent()) {
                log.info("Duplicate LikeChangedEvent. Skipping. eventId={}", event.eventId());
                ack.acknowledge();
                return;
            }

            Cache detailCache = cacheManager.getCache(RedisCacheConfig.CACHE_PRODUCT_DETAIL);
            Cache listCache = cacheManager.getCache(RedisCacheConfig.CACHE_PRODUCT_LIST);

            detailCache.evict(event.productId());
            listCache.clear();

            log.info("Cache evicted for productId={} (like changed)", event.productId());

            eventHandledRepository.saveAndFlush(new EventHandled(event.eventId(), CONSUMER_NAME));
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process LikeChangedEvent: {}", event, e);
        }
    }
}
