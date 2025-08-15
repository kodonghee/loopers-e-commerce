package com.loopers.application.product;

import com.loopers.config.redis.RedisCacheConfig;
import com.loopers.domain.brand.BrandReader;
import com.loopers.domain.like.ProductLikeSummary;
import com.loopers.domain.like.ProductLikeSummaryRepository;
import com.loopers.domain.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.loopers.config.redis.RedisCacheConfig.CACHE_PRODUCT_LIST;

@RequiredArgsConstructor
@Service
public class ProductFacade {

    private final ProductRepository productRepository;
    private final BrandReader brandReader;
    private final ProductLikeSummaryRepository productLikeSummaryRepository;

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

        return saved;
    }

    @Cacheable(
            cacheNames = CACHE_PRODUCT_LIST,
            key = "'brand:' + (#condition.getBrandId() != null ? #condition.getBrandId() : 'ALL')" +
                    " + ':sort:' + #condition.getSortType()" +
                    " + ':p:' + #condition.getPage()" +
                    " + ':s:' + #condition.getSize()",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public List<ProductResult> getProductList(ProductSearchCondition condition) {
        List<ProductListView> rows = productRepository.findListViewByCondition(condition);
        return rows.stream().map(ProductMapper::from).toList();
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
        return ProductMapper.fromProduct(product, brandName, likeCount);
    }
}
