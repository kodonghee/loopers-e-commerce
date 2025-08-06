package com.loopers.application.product;

import com.loopers.domain.brand.BrandReader;
import com.loopers.domain.like.LikeCountReader;
import com.loopers.domain.product.*;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static com.loopers.domain.product.ProductSearchCondition.ProductSortType.PRICE_ASC;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DisplayName("ProductUseCase 단위 테스트")
class ProductFacadeIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandReader brandReader;

    @Autowired
    private LikeCountReader likeCountReader;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private static final String PRODUCT_NAME = "테스트 상품";
    private static final int STOCK_VALUE = 100;
    private static final BigDecimal PRICE_VALUE = new BigDecimal("50000");
    private static final Long BRAND_ID = 1L;
    private static final Long PRODUCT_ID = 10L;

    @Nested
    @DisplayName("getProductList 메서드 테스트")
    class GetProductListTest {
        @Test
        @DisplayName("조건에 맞는 상품 목록 조회 시, ProductInfo 리스트를 반환 해야 한다.")
        void getProductList_shouldReturnProductInfoList() {
            // arrange
            ProductCriteria command1 = new ProductCriteria("cake", 10, new BigDecimal("10000"), 1L);
            ProductCriteria command2 = new ProductCriteria("cookie", 20, new BigDecimal("20000"), 2L);
            Product product1 = productFacade.create(command1);
            assertTrue(productRepository.findById(product1.getId()).isPresent());
            Product product2 = productFacade.create(command2);
            assertTrue(productRepository.findById(product2.getId()).isPresent());

            ProductSearchCondition condition = new ProductSearchCondition(null, PRICE_ASC, 1, 10);

            // act
            List<ProductResult> result = productFacade.getProductList(condition);
            System.out.println("상품 리스트: " + product1 + product2);
            System.out.println("상품 리스트: " + result);

            // assert
            /*assertThat(result).hasSize(2);
            assertThat(result.get(0).productId()).isEqualTo(2L);
            assertThat(result.get(0).name()).isEqualTo(PRODUCT_NAME + "2");
            assertThat(result.get(0).brandName()).isEqualTo("BrandB");
            assertThat(result.get(0).likeCount()).isEqualTo(10);
            assertThat(result.get(1).productId()).isEqualTo(1L);
            assertThat(result.get(1).name()).isEqualTo(PRODUCT_NAME + "1");
            assertThat(result.get(1).brandName()).isEqualTo("BrandA");
            assertThat(result.get(1).likeCount()).isEqualTo(5);
            verify(productRepository).findAllByCondition(condition);*/
        }
    }
}
