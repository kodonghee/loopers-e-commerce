package com.loopers.application.product;

import com.loopers.domain.brand.BrandReader;
import com.loopers.domain.like.LikeCountReader;
import com.loopers.domain.product.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductUseCase 단위 테스트")
class ProductFacadeTest {

    @InjectMocks
    private ProductFacade productFacade;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandReader brandReader;

    @Mock
    private LikeCountReader likeCountReader;

    private static final String PRODUCT_NAME = "테스트 상품";
    private static final int STOCK_VALUE = 100;
    private static final BigDecimal PRICE_VALUE = new BigDecimal("50000");
    private static final Long BRAND_ID = 1L;
    private static final Long PRODUCT_ID = 10L;

    @Nested
    @DisplayName("create 메서드 테스트")
    class CreateTest {
        @Test
        @DisplayName("상품 생성 요청 시, Product 객체를 생성하고 저장해야 한다.")
        void create_shouldCreateAndSaveProduct() {
            // arrange
            ProductCriteria command = new ProductCriteria(PRODUCT_NAME, STOCK_VALUE, PRICE_VALUE, BRAND_ID);
            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

            // act
            productFacade.create(command);

            // assert
            verify(productRepository).save(productCaptor.capture());
            Product savedProduct = productCaptor.getValue();
            assertThat(savedProduct.getName()).isEqualTo(PRODUCT_NAME);
            assertThat(savedProduct.getStock().getValue()).isEqualTo(STOCK_VALUE);
            assertThat(savedProduct.getPrice().getAmount()).isEqualTo(PRICE_VALUE);
            assertThat(savedProduct.getBrandId()).isEqualTo(BRAND_ID);
        }

        @Test
        @DisplayName("상품 생성 시 상품명이 null이면 IllegalArgumentException을 던져야 한다.")
        void create_shouldThrowException_whenNameIsNull() {
            // arrange
            ProductCriteria command = new ProductCriteria(null, STOCK_VALUE, PRICE_VALUE, BRAND_ID);

            // act & assert
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> productFacade.create(command));
            assertThat(thrown.getMessage()).isEqualTo("상품명은 필수입니다.");
        }

        @Test
        @DisplayName("상품 생성 시 상품명이 공백이면 IllegalArgumentException을 던져야 한다.")
        void create_shouldThrowException_whenNameIsBlank() {
            // arrange
            ProductCriteria command = new ProductCriteria(" ", STOCK_VALUE, PRICE_VALUE, BRAND_ID);

            // act & assert
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> productFacade.create(command));
            assertThat(thrown.getMessage()).isEqualTo("상품명은 필수입니다.");
        }

        @Test
        @DisplayName("상품 생성 시 brandId가 null이면 IllegalArgumentException을 던져야 한다.")
        void create_shouldThrowException_whenBrandIdIsNull() {
            // arrange
            ProductCriteria command = new ProductCriteria(PRODUCT_NAME, STOCK_VALUE, PRICE_VALUE, null);

            // act & assert
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> productFacade.create(command));
            assertThat(thrown.getMessage()).isEqualTo("브랜드 ID는 필수입니다.");
        }

        @Test
        @DisplayName("상품 생성 시 brandId가 0 이하이면 IllegalArgumentException을 던져야 한다.")
        void create_shouldThrowException_whenBrandIdIsInvalid() {
            // arrange
            ProductCriteria command = new ProductCriteria(PRODUCT_NAME, STOCK_VALUE, PRICE_VALUE, 0L);

            // act & assert
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> productFacade.create(command));
            assertThat(thrown.getMessage()).isEqualTo("브랜드 ID는 필수입니다.");
        }
    }

    @Nested
    @DisplayName("getProductList 메서드 테스트")
    class GetProductListTest {
        @Test
        @DisplayName("조건에 맞는 상품 목록 조회 시, ProductInfo 리스트를 반환해야 한다.")
        void getProductList_shouldReturnProductInfoList() {
            // arrange
            ProductSearchCondition condition = new ProductSearchCondition(null, null, 1, 10);

            // Mock Product 객체 생성 및 스텁 설정
            Product mockProduct1 = mock(Product.class);
            when(mockProduct1.getId()).thenReturn(1L);
            when(mockProduct1.getName()).thenReturn(PRODUCT_NAME + "1");
            when(mockProduct1.getStock()).thenReturn(new Stock(10));
            when(mockProduct1.getPrice()).thenReturn(new Money(new BigDecimal("1000")));
            when(mockProduct1.getBrandId()).thenReturn(BRAND_ID);

            Product mockProduct2 = mock(Product.class);
            when(mockProduct2.getId()).thenReturn(2L);
            when(mockProduct2.getName()).thenReturn(PRODUCT_NAME + "2");
            when(mockProduct2.getStock()).thenReturn(new Stock(20));
            when(mockProduct2.getPrice()).thenReturn(new Money(new BigDecimal("2000")));
            when(mockProduct2.getBrandId()).thenReturn(BRAND_ID + 1);

            List<Product> mockProducts = List.of(mockProduct1, mockProduct2);

            when(productRepository.findAllByCondition(any())).thenReturn(mockProducts);
            when(brandReader.getBrandName(BRAND_ID)).thenReturn("BrandA");
            when(likeCountReader.getLikeCountByProductId(mockProduct1.getId())).thenReturn(5);
            when(brandReader.getBrandName(BRAND_ID + 1)).thenReturn("BrandB");
            when(likeCountReader.getLikeCountByProductId(mockProduct2.getId())).thenReturn(10);

            // act
            List<ProductResult> result = productFacade.getProductList(condition);

            // assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).productId()).isEqualTo(1L);
            assertThat(result.get(0).name()).isEqualTo(PRODUCT_NAME + "1");
            assertThat(result.get(0).brandName()).isEqualTo("BrandA");
            assertThat(result.get(0).likeCount()).isEqualTo(5);
            assertThat(result.get(1).productId()).isEqualTo(2L);
            assertThat(result.get(1).name()).isEqualTo(PRODUCT_NAME + "2");
            assertThat(result.get(1).brandName()).isEqualTo("BrandB");
            assertThat(result.get(1).likeCount()).isEqualTo(10);
            verify(productRepository).findAllByCondition(condition);
        }

        @Test
        @DisplayName("조건에 맞는 상품이 없을 때, 빈 리스트를 반환해야 한다.")
        void getProductList_shouldReturnEmptyList_whenNoProductsFound() {
            // arrange
            ProductSearchCondition condition = new ProductSearchCondition(null, null, 1, 10);
            when(productRepository.findAllByCondition(any())).thenReturn(Collections.emptyList());

            // act
            List<ProductResult> result = productFacade.getProductList(condition);

            // assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getProductDetail 메서드 테스트")
    class GetProductDetailTest {
        @Test
        @DisplayName("존재하는 상품 ID로 조회 시, ProductInfo 객체를 반환해야 한다.")
        void getProductDetail_shouldReturnProductInfo_whenProductExists() {
            // arrange
            // Mock Product 객체 생성 및 스텁 설정
            Product mockProduct = mock(Product.class);
            when(mockProduct.getId()).thenReturn(PRODUCT_ID);
            when(mockProduct.getName()).thenReturn(PRODUCT_NAME);
            when(mockProduct.getStock()).thenReturn(new Stock(STOCK_VALUE));
            when(mockProduct.getPrice()).thenReturn(new Money(PRICE_VALUE));
            when(mockProduct.getBrandId()).thenReturn(BRAND_ID);

            // productRepository.findById()가 Optional.of(mockProduct)를 반환하도록 올바르게 스텁
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));
            when(brandReader.getBrandName(BRAND_ID)).thenReturn("Test Brand");
            when(likeCountReader.getLikeCountByProductId(PRODUCT_ID)).thenReturn(10);

            // act
            ProductResult result = productFacade.getProductDetail(PRODUCT_ID);

            // assert
            assertThat(result.productId()).isEqualTo(PRODUCT_ID);
            assertThat(result.name()).isEqualTo(PRODUCT_NAME);
            assertThat(result.stock()).isEqualTo(STOCK_VALUE);
            assertThat(result.price()).isEqualTo(PRICE_VALUE);
            assertThat(result.brandName()).isEqualTo("Test Brand");
            assertThat(result.likeCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 조회 시, IllegalArgumentException을 던져야 한다.")
        void getProductDetail_shouldThrowException_whenProductDoesNotExist() {
            // arrange
            when(productRepository.findById(any())).thenReturn(Optional.empty());

            // act & assert
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> productFacade.getProductDetail(PRODUCT_ID));

            assertThat(thrown.getMessage()).isEqualTo("존재하지 않는 상품입니다.");
        }
    }
}
