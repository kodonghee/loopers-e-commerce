package com.loopers.application.ranking;

import com.loopers.application.product.ProductMapper;
import com.loopers.application.product.ProductResult;
import com.loopers.domain.brand.BrandReader;
import com.loopers.domain.like.ProductLikeSummary;
import com.loopers.domain.like.ProductLikeSummaryRepository;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.ranking.DailyRankingRepository;
import com.loopers.domain.ranking.PeriodicRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RankingService {

    private final DailyRankingRepository dailyRankingRepository;
    private final PeriodicRankingRepository periodicRankingRepository;
    private final ProductRepository productRepository;
    private final BrandReader brandReader;
    private final ProductLikeSummaryRepository likeSummaryRepository;

    public List<ProductResult> getDailyTopProducts(LocalDate date, int page, int size) {
        int start = (page - 1) * size;
        int end = start + size - 1;

        List<String> productIds = dailyRankingRepository.getTopProducts(date, start, end);
        return productRepository.findAllById(
                        productIds.stream().map(Long::valueOf).toList()
                ).stream()
                .map(product -> {
                    String brandName = brandReader.getBrandName(product.getBrandId());
                    Long likeCount = likeSummaryRepository.findByProductId(product.getId())
                            .map(ProductLikeSummary::getLikeCount)
                            .orElse(0L);
                    Long rank = dailyRankingRepository.getRank(date, product.getId().toString());

                    return ProductMapper.fromProduct(product, brandName, likeCount, rank);
                })
                .toList();
    }

    public List<ProductResult> getWeeklyTopProducts(int page, int size) {
        return periodicRankingRepository.getWeeklyTopProducts(page, size);
    }

    public List<ProductResult> getMonthlyTopProducts(int page, int size) {
        return periodicRankingRepository.getMonthlyTopProducts(page, size);
    }

    public Long getProductRank(LocalDate date, Long productId) {
        return dailyRankingRepository.getRank(date, productId.toString());
    }
}
