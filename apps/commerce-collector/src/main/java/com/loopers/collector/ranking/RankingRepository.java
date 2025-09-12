package com.loopers.collector.ranking;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RankingRepository {

    private final StringRedisTemplate redisTemplate;

    public void incrementScore(LocalDate date, String productId, double score) {
        String key = RankingKeyGenerator.dailyKey(date);

        redisTemplate.opsForZSet().incrementScore(key, productId, score);

        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, RankingKeyGenerator.ttlSeconds(), TimeUnit.SECONDS);
        }
    }
}
