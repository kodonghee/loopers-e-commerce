package com.loopers.infrastructure.like;

import com.loopers.domain.like.ProductLikeSummary;
import com.loopers.domain.like.ProductLikeSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductLikeSummaryRepositoryImpl implements ProductLikeSummaryRepository {

    private final ProductLikeSummaryJpaRepository productLikeSummaryJpaRepository;

    @Override
    public Optional<ProductLikeSummary> findByProductId(Long productId) {
        return productLikeSummaryJpaRepository.findById(productId);
    }

    @Override
    public Optional<ProductLikeSummary> findByProductIdForUpdate(Long productId) {
        return productLikeSummaryJpaRepository.findByProductIdForUpdate(productId);
    }

    @Override
    public void save(ProductLikeSummary summary) {
        productLikeSummaryJpaRepository.save(summary);
    }

    @Override
    public void ensureRow(Long productId) {
        productLikeSummaryJpaRepository.ensureRow(productId);
    }
}
