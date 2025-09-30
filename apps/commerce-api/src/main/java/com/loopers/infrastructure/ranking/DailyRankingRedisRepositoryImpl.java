package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.DailyRankingRepository;
import com.loopers.support.ranking.RankingKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class DailyRankingRedisRepositoryImpl implements DailyRankingRepository {

    private final StringRedisTemplate redisTemplate;

    @Override
    public List<String> getTopProducts(LocalDate date, int start, int end) {
        String key = RankingKeyGenerator.dailyKey(date);
        Set<String> range = redisTemplate.opsForZSet().reverseRange(key, start, end);
        return range == null ? List.of() : List.copyOf(range);
    }

    @Override
    public Long getRank(LocalDate date, String productId) {
        String key = RankingKeyGenerator.dailyKey(date);
        Long rank = redisTemplate.opsForZSet().reverseRank(key, productId);
        return rank == null ? null : rank + 1;
    }
}
