package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.*;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
    private DatabaseCleanUp databaseCleanUp;

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
}
