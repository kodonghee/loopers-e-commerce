package com.loopers.application.product;

import com.loopers.application.event.MessagePublisher;
import com.loopers.config.redis.RedisCacheConfig;
import com.loopers.domain.brand.BrandReader;
import com.loopers.domain.like.ProductLikeSummary;
import com.loopers.domain.like.ProductLikeSummaryRepository;
import com.loopers.domain.product.*;
import com.loopers.domain.ranking.RankingRepository;
import com.loopers.events.view.ProductViewedEvent;
import com.loopers.infrastructure.product.ProductListCache;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductFacade {

    private final ProductRepository productRepository;
    private final RankingRepository rankingRepository;
    private final BrandReader brandReader;
    private final ProductLikeSummaryRepository productLikeSummaryRepository;
    private final ProductListCache productListCache;
    private final MessagePublisher messagePublisher;

    @Transactional
    public Product create(ProductCriteria command) {

        Product product = new Product(
                command.name(),
                new Stock(command.stockValue()),
                new Money(command.priceValue()),
                command.brandId()
        );

        Product saved = productRepository.save(product);
        productLikeSummaryRepository.ensureRow(saved.getId(), saved.getBrandId());
        productListCache.evictByBrand(saved.getBrandId());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<ProductResult> getProductList(ProductSearchCondition condition) {
        // 1) 캐시 조회
        var cached = productListCache.get(condition);
        if (cached.isPresent()) return cached.get();
        // 2) DB 조회
        List<ProductListView> rows = productRepository.findListViewByCondition(condition);
        List<ProductResult> result = rows.stream().map(ProductMapper::from).toList();
        // 3) 캐시 저장 (빈 리스트는 저장하지 않음)
        productListCache.put(condition, result);
        return result;
    }

    @Cacheable(cacheNames = RedisCacheConfig.CACHE_PRODUCT_DETAIL, key = "#productId", unless = "#result == null")
    @Transactional(readOnly = true)
    public ProductResult getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        String brandName = brandReader.getBrandName(product.getBrandId());
        Long likeCount = productLikeSummaryRepository.findByProductId(productId)
                .map(ProductLikeSummary::getLikeCount)
                .orElse(0L);

        Long rank = rankingRepository.getRank(LocalDate.now(), productId.toString());

        messagePublisher.publish(ProductViewedEvent.of(productId));

        return ProductMapper.fromProduct(product, brandName, likeCount, rank);
    }
}
