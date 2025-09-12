package com.loopers.domain.ranking;

import java.time.LocalDate;
import java.util.List;

public interface RankingRepository {
    void incrementScore(LocalDate date, String productId, double score);
    List<String> getTopProducts(LocalDate date, int start, int end);
    Long getRank(LocalDate date, String productId);
}
