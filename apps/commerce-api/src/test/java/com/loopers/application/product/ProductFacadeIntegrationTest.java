package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.like.ProductLikeSummary;
import com.loopers.domain.like.ProductLikeSummaryRepository;
import com.loopers.domain.product.*;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("상품 도메인 통합 테스트")
class ProductFacadeIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private ProductLikeSummaryRepository productLikeSummaryRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    CacheManager cacheManager;

    private Long brandId;

    @BeforeEach
    void setup() {
        Brand brand = brandJpaRepository.save(new Brand("Paris Baguette"));
        this.brandId = brand.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @AfterEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(n -> {
            Cache c = cacheManager.getCache(n);
            if (c != null) c.clear();
        });
    }

    @Test
    @DisplayName("상품 생성 시, 정상으로 저장 된다.")
    void createProduct_success() {
        // arrange
        ProductCriteria criteria = new ProductCriteria("cake", 10, new BigDecimal("10000"), brandId);

        // act
        Product savedProduct = productFacade.create(criteria);

        // assert
        Product found = productRepository.findById(savedProduct.getId())
                .orElseThrow(() -> new AssertionError("상품이 저장되지 않았습니다."));

        assertThat(found.getName()).isEqualTo("cake");
        assertThat(found.getStock().getValue()).isEqualTo(10);
        assertThat(found.getPrice().getAmount().compareTo(new BigDecimal("10000"))).isZero();
        assertThat(found.getBrandId()).isEqualTo(brandId);
    }

    @Test
    @DisplayName("상품 상세 조회 시, 브랜드명과 좋아요 수 포함한 정보를 반환한다.")
    void getProductDetail_success() {
        // arrange
        ProductCriteria criteria = new ProductCriteria("coffee", 20, new BigDecimal("4500"), brandId);
        Product savedProduct = productFacade.create(criteria);

        ProductLikeSummary likeSummary = new ProductLikeSummary(savedProduct.getId(), brandId);
        likeSummary.increment();
        productLikeSummaryRepository.save(likeSummary);

        // act
        ProductResult result = productFacade.getProductDetail(savedProduct.getId());

        // assert
        assertThat(result.name()).isEqualTo("coffee");
        assertThat(result.stock()).isEqualTo(20);
        assertThat(result.price()).isEqualByComparingTo(new BigDecimal("4500"));
        assertThat(result.brandName()).isEqualTo("Paris Baguette");
        assertThat(result.likeCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("최신순 정렬로 상품 목록을 반환할 수 있다.")
    void getProductList_sortByLatest() {
        // arrange
        ProductCriteria product1 = new ProductCriteria("americano", 10, new BigDecimal("4000"), brandId);
        ProductCriteria product2 = new ProductCriteria("latte", 15, new BigDecimal("4500"), brandId);

        productFacade.create(product1);
        productFacade.create(product2);

        ProductSearchCondition condition = new ProductSearchCondition(
                brandId,
                ProductSearchCondition.ProductSortType.LATEST,
                0, 10
        );

        // act
        List<ProductResult> results = productFacade.getProductList(condition);

        // assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("latte");
        assertThat(results.get(1).name()).isEqualTo("americano");
    }

    @Test
    @DisplayName("가격 오름차순 정렬로 상품 목록을 반환할 수 있다.")
    void getProductList_sortByPriceAsc() {
        // arrange
        ProductCriteria deli1 = new ProductCriteria("americano", 10, new BigDecimal("5000"), brandId);
        ProductCriteria deli2 = new ProductCriteria("latte", 15, new BigDecimal("3000"), brandId);
        ProductCriteria deli3 = new ProductCriteria("bread", 20, new BigDecimal("10000"), brandId);

        productFacade.create(deli1);
        productFacade.create(deli2);
        productFacade.create(deli3);

        ProductSearchCondition condition = new ProductSearchCondition(
                brandId,
                ProductSearchCondition.ProductSortType.PRICE_ASC,
                0, 10
        );

        // act
        List<ProductResult> results = productFacade.getProductList(condition);

        // assert
        assertThat(results).hasSize(3);
        assertThat(results.get(0).name()).isEqualTo("latte");
        assertThat(results.get(1).name()).isEqualTo("americano");
        assertThat(results.get(2).name()).isEqualTo("bread");
    }

    @Test
    @DisplayName("좋아요 수 내림차순 정렬로 상품 목록을 반환할 수 있다.")
    void getProductList_sortByLikesDesc() {
        // arrange
        Product product1 = productFacade.create(new ProductCriteria("shoes", 10, new BigDecimal("10000"), brandId));
        Product product2 = productFacade.create(new ProductCriteria("bag", 5, new BigDecimal("20000"), brandId));

        // 상품 1에 좋아요 2개, 상품 2에 좋아요 1개
        ProductLikeSummary likeSummary1 = new ProductLikeSummary(product1.getId(), brandId);
        ProductLikeSummary likeSummary2 = new ProductLikeSummary(product2.getId(), brandId);
        likeSummary1.increment();
        likeSummary2.increment();
        likeSummary2.increment();
        productLikeSummaryRepository.save(likeSummary1);
        productLikeSummaryRepository.save(likeSummary2);

        ProductSearchCondition condition = new ProductSearchCondition(
                brandId,
                ProductSearchCondition.ProductSortType.LIKES_DESC,
                0, 10
        );

        // act
        List<ProductResult> results = productFacade.getProductList(condition);

        // assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("bag");
        assertThat(results.get(1).name()).isEqualTo("shoes");
    }

    @Test
    @DisplayName("상품 목록 조회 시, 브랜드 ID 필터가 적용된다.")
    void getProductList_withBrandFilter() {
        // arrange
        Brand otherBrand = brandJpaRepository.save(new Brand("Starbucks"));
        Long otherBrandId = otherBrand.getId();

        productFacade.create(new ProductCriteria("cake", 10, new BigDecimal("10000"), brandId));
        productFacade.create(new ProductCriteria("bread", 15, new BigDecimal("8000"), brandId));

        productFacade.create(new ProductCriteria("coffee", 5, new BigDecimal("5000"), otherBrandId));

        ProductSearchCondition condition = new ProductSearchCondition(
                brandId,
                ProductSearchCondition.ProductSortType.LATEST,
                0, 10
        );

        // act
        List<ProductResult> results = productFacade.getProductList(condition);

        // assert
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(ProductResult::brandName)
                .allMatch(name -> name.equals("Paris Baguette"));
    }
}
