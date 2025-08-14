package com.loopers.domain.like;

import java.util.Optional;

public interface ProductLikeSummaryRepository {
    Optional<ProductLikeSummary> findByProductId(Long productId);
    void save(ProductLikeSummary summary);
    Optional<ProductLikeSummary> findByProductIdForUpdate(Long productId);

    void ensureRow(Long productId);
}
