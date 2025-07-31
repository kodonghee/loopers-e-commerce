package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Product 도메인 단위 테스트")
public class ProductTest {
    @DisplayName("상품 생성 단위 테스트")
    @Nested
    class CreateProduct {
        @DisplayName("재고가 음수이면 상품이 생성되지 않는다.")
        @Test
        void failToCreateProduct_whenStockIsNegative() {
            // arrange
            String name = "인피니티건틀렛";
            Money price = new Money(BigDecimal.valueOf(200000));
            Long brandId = 1L;

            // act & assert
            assertThatThrownBy(() -> new Product(name, new Stock(-5), price, brandId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("재고는 0 이상이어야 합니다.");
        }

        @DisplayName("가격이 음수이면 상품이 생성되지 않는다.")
        @Test
        void failToCreateProduct_whenPriceIsNegative() {
            // arrange
            String name = "인피니티건틀렛";
            Stock stock = new Stock(10);
            Long brandId = 1L;

            // act & Assert
            assertThatThrownBy(() -> new Product(name, stock, new Money(BigDecimal.valueOf(-200000)), brandId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("가격은 0보다 커야 합니다.");
        }

        @DisplayName("정상적인 값이 주어지면 상품이 생성 된다.")
        @Test
        void createProduct_whenValidValuesGiven() {
            // arrange
            String name = "인피니티건틀렛";
            Stock stock = new Stock(10);
            Money price = new Money(BigDecimal.valueOf(200000));
            Long brandId = 1L;

            // act
            Product product = new Product(name, stock, price, brandId);

            // assert
            assertThat(product.getName()).isEqualTo("인피니티건틀렛");
            assertThat(product.getStock().getValue()).isEqualTo(10);
            assertThat(product.getPrice().getAmount()).isEqualTo(BigDecimal.valueOf(200000));
        }
    }

    @DisplayName("재고 차감 단위 테스트")
    @Nested
    class DecreaseStock {
        @DisplayName("차감 수량이 상품 재고보다 크면 재고를 차감할 수 없다.")
        @Test
        void failToDecreaseStock_whenDecreasingMoreThanStock() {
            // arrange
            Product product = new Product("인피니티건틀렛", new Stock(2), new Money(BigDecimal.valueOf(200000)), 1L);
            int quantityToDecrease = 5;

            // act & assert
            assertThatThrownBy(() -> product.decreaseStock(quantityToDecrease))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("재고가 부족 합니다.");
        }

        @DisplayName("차감 수량이 0이면 재고를 차감할 수 없다. (경계값)")
        @Test
        void failToDecreaseStock_whenDecreasingZero() {
            // arrange
            Product product = new Product("인피니티건틀렛", new Stock(10), new Money(BigDecimal.valueOf(200000)), 1L);
            int quantityToDecrease = 0;

            // act & assert
            assertThatThrownBy(() -> product.decreaseStock(quantityToDecrease))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("재고 차감량은 1 이상이어야 합니다.");
        }

        @DisplayName("차감 수량이 음수이면 재고를 차감할 수 없다.")
        @Test
        void failToDecreaseStock_whenDecreasingNegativeQuantity() {
            // arrange
            Product product = new Product("인피니티건틀렛", new Stock(10), new Money(BigDecimal.valueOf(200000)), 1L);
            int quantityToDecrease = -3;

            // act & assert
            assertThatThrownBy(() -> product.decreaseStock(quantityToDecrease))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("재고 차감량은 1 이상이어야 합니다.");
        }

        @DisplayName("재고가 충분 하면 차감에 성공 한다.")
        @Test
        void DecreaseStock_whenEnoughStock() {
            // arrange
            Product product = new Product("인피니티건틀렛", new Stock(10), new Money(BigDecimal.valueOf(200000)), 1L);
            int quantityToDecrease = 3;

            // act
            product.decreaseStock(quantityToDecrease);

            // assert
            assertThat(product.getStock().getValue()).isEqualTo(7);
        }
    }
}
