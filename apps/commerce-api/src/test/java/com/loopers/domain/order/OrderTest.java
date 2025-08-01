package com.loopers.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OrderTest {

    @Nested
    @DisplayName("주문 엔티티 생성")
    class CreateOrder {

        @Test
        @DisplayName("유효한 값으로 주문 엔티티 생성 시 성공한다")
        void createOrder_withValidValues_shouldSucceed() {
            // Arrange
            String userId = "user1";
            List<OrderItem> orderItems = List.of(
                    new OrderItem(1L, 2, new BigDecimal("1000")),
                    new OrderItem(2L, 1, new BigDecimal("500"))
            );

            // Act & Assert
            assertDoesNotThrow(() -> new Order(userId, orderItems));
        }

        @Test
        @DisplayName("주문 총 금액을 올바르게 계산한다")
        void getTotalAmount_shouldCalculateCorrectly() {
            // Arrange
            String userId = "user1";
            List<OrderItem> orderItems = List.of(
                    new OrderItem(1L, 2, new BigDecimal("1000")),
                    new OrderItem(2L, 1, new BigDecimal("500"))
            );
            Order order = new Order(userId, orderItems);

            // Act
            BigDecimal totalAmount = order.getTotalAmount();

            // Assert
            assertThat(totalAmount).isEqualTo(new BigDecimal("2500"));
        }
    }
}
