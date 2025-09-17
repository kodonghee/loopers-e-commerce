package com.loopers.application.product;

import com.loopers.application.like.LikeCriteria;
import com.loopers.application.like.LikeFacade;
import com.loopers.config.redis.RedisCacheConfig;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.like.ProductLikeSummaryRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("상품 상세 캐시 통합 테스트")
class ProductDetailCacheIntegrationTest {

    @Autowired private ProductFacade productFacade;
    @Autowired private LikeFacade likeFacade;

    @Autowired private ProductRepository productRepository;
    @Autowired private BrandJpaRepository brandJpaRepository;
    @Autowired private ProductLikeSummaryRepository productLikeSummaryRepository;

    @Autowired private RedisTemplate<String, String> redisTemplate;
    @Autowired private DatabaseCleanUp databaseCleanUp;

    private Long brandId;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();
        Brand brand = brandJpaRepository.save(new Brand("Paris Baguette"));
        this.brandId = brand.getId();

        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    @DisplayName("상품 상세 1회 호출 후 캐시가 적재 되고 TTL이 설정 된다.")
    void detail_first_call_warms_cache_and_sets_ttl() {
        // arrange
        ProductCriteria criteria = new ProductCriteria("coffee", 20, new BigDecimal("4500"), brandId);
        Product saved = productFacade.create(criteria);
        Long id = saved.getId();

        // act: 첫 호출(미스 → 저장), 두 번째 호출(히트)
        ProductResult a = productFacade.getProductDetail(id);
        ProductResult b = productFacade.getProductDetail(id);

        // assert
        assertThat(b).usingRecursiveComparison().isEqualTo(a);

        // assert: 캐시 키 존재 + TTL > 0
        String key = RedisCacheConfig.CACHE_PRODUCT_DETAIL + "::" + id;
        assertThat(redisTemplate.hasKey(key)).isTrue();
        Long ttlSec = redisTemplate.getExpire(key);
        assertThat(ttlSec).isNotNull().isGreaterThan(0L);
    }

    @Test
    @DisplayName("중복 좋아요(변경 없음) 시 캐시 무효화가 발생하지 않는다")
    void duplicate_like_does_not_evict() {
        // arrange
        Product saved = productFacade.create(new ProductCriteria("bag", 5, new BigDecimal("20000"), brandId));
        Long id = saved.getId();

        // 캐시 적재
        productFacade.getProductDetail(id);
        String key = RedisCacheConfig.CACHE_PRODUCT_DETAIL + "::" + id;
        assertThat(redisTemplate.hasKey(key)).isTrue();
        Long ttlBefore = redisTemplate.getExpire(key);
        assertThat(ttlBefore).isNotNull().isGreaterThan(0L);

        // act: 첫 좋아요(성공)로 캐시 무효화 → 재적재
        assertThat(likeFacade.likeProduct(new LikeCriteria("user-1", id))).isTrue();
        // 재호출로 재적재
        productFacade.getProductDetail(id);
        assertThat(redisTemplate.hasKey(key)).isTrue();

        Long ttlAfterFirst = redisTemplate.getExpire(key);
        assertThat(ttlAfterFirst).isNotNull().isGreaterThan(0L);

        // act: 같은 유저가 다시 좋아요(중복) → changed=false → Evict 조건 불충족
        boolean changed = likeFacade.likeProduct(new LikeCriteria("user-1", id));
        assertThat(changed).isFalse();

        // then: 캐시가 유지(키 존재 + TTL 여전히 > 0)
        assertThat(redisTemplate.hasKey(key)).isTrue();
        Long ttlAfterDup = redisTemplate.getExpire(key);
        assertThat(ttlAfterDup).isNotNull().isGreaterThan(0L);
    }
}
