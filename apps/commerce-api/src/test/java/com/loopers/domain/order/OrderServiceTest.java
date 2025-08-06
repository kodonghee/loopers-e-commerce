package com.loopers.domain.order;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Money;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSearchCondition;
import com.loopers.domain.product.Stock;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {

    private ProductRepository productRepository;
    private PointRepository pointRepository;
    private OrderService orderService;

    private static class FakeProductRepository implements ProductRepository {
        private final Map<Long, Product> products = new HashMap<>();
        private final AtomicLong idGenerator = new AtomicLong(0L);

        public Product save(Long id, Product product) {

            products.put(id, product);
            return product;
        }

        @Override
        public Optional<Product> findById(Long productId) {
            return Optional.ofNullable(products.get(productId));
        }

        @Override
        public Product save(Product product) {
            long newId = idGenerator.incrementAndGet();
            products.put(newId, product);

            return product;
        }

        @Override
        public List<Product> findAll() {
            return List.copyOf(products.values());
        }

        @Override
        public List<Product> findAllByCondition(ProductSearchCondition condition) {
            return List.of();
        }

        @Override
        public List<Product> findAllById(List<Long> ids) {
            return null;
        }
    }

    private static class FakePointRepository implements PointRepository {
        private final Map<UserId, Point> points = new HashMap<>();

        @Override
        public Optional<Point> find(UserId userId) {
            return Optional.ofNullable(points.get(userId));
        }

        @Override
        public Point save(Point point) {
            points.put(new UserId(point.getUserId()), point);
            return point;
        }

        @Override
        public boolean existsByUserId(UserId userId) {
            return points.containsKey(userId);
        }
    }

    @BeforeEach
    void setUp() {
        productRepository = new FakeProductRepository();
        pointRepository = new FakePointRepository();
        orderService = new OrderService(productRepository, pointRepository);
    }

    @Nested
    @DisplayName("주문 생성 테스트")
    class CreateOrder {

        private final String userId = "user1";
        private final UserId userIdObj = new UserId(userId);
        private final Long productId1 = 1L;
        private final Long productId2 = 2L;
        private final BigDecimal price1 = new BigDecimal("1000");
        private final BigDecimal price2 = new BigDecimal("500");
        private final int stock1 = 10;
        private final int stock2 = 5;

        @BeforeEach
        void initData() {
            ((FakeProductRepository) productRepository).save(productId1, new Product("item1", new Stock(stock1), new Money(price1), 1L));
            ((FakeProductRepository) productRepository).save(productId2, new Product("item2", new Stock(stock2), new Money(price2), 2L));

            ((FakePointRepository) pointRepository).save(new Point(userId, new BigDecimal("5000"))); // 충분한 포인트
        }

        @Test
        @DisplayName("주문 생성 시, 재고와 포인트가 충분하면 성공적으로 주문 객체를 반환한다")
        void createOrder_withSufficientStockAndPoints_shouldSucceed() {
            // Arrange
            List<OrderItem> items = List.of(
                    new OrderItem(productId1, 2, price1),
                    new OrderItem(productId2, 1, price2)
            );

            // Act
            Order createdOrder = orderService.createOrder(userId, items);

            // Assert
            assertThat(createdOrder.getUserId()).isEqualTo(userId);
            assertThat(createdOrder.getOrderItems()).hasSize(2);
            assertThat(createdOrder.getTotalAmount()).isEqualTo(
                    BigDecimal.valueOf(2).multiply(price1).add(BigDecimal.valueOf(1).multiply(price2))
            );
        }

        @Test
        @DisplayName("주문 상품의 재고가 부족하면 CoreException을 던진다")
        void createOrder_withInsufficientStock_shouldThrowException() {
            // Arrange
            List<OrderItem> items = List.of(
                    new OrderItem(productId1, 11, price1)
            );

            // Act & Assert
            CoreException exception = assertThrows(CoreException.class, () ->
                    orderService.createOrder(userId, items)
            );
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("주문 총액이 포인트보다 부족하면 CoreException을 던진다")
        void createOrder_withInsufficientPoints_shouldThrowException() {
            // Arrange (준비)
            List<OrderItem> items = List.of(
                    new OrderItem(productId1, 3, price1)
            );
            ((FakePointRepository) pointRepository).save(new Point(userId, new BigDecimal("2000")));

            // Act & Assert (실행 및 검증)
            CoreException exception = assertThrows(CoreException.class, () ->
                    orderService.createOrder(userId, items)
            );
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("주문하려는 상품이 존재하지 않으면 CoreException을 던진다")
        void createOrder_withNonexistentProduct_shouldThrowException() {
            // Arrange (준비)
            Long nonExistentProductId = 99L;
            List<OrderItem> items = List.of(
                    new OrderItem(nonExistentProductId, 1, new BigDecimal("100"))
            );

            // Act & Assert (실행 및 검증)
            CoreException exception = assertThrows(CoreException.class, () ->
                    orderService.createOrder(userId, items)
            );
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("주문하려는 사용자의 포인트 정보가 없으면 CoreException을 던진다")
        void createOrder_withNonexistentUserPoints_shouldThrowException() {
            // Arrange (준비)
            String nonExistentUserId = "user99";
            List<OrderItem> items = List.of(
                    new OrderItem(productId1, 1, price1)
            );

            ((FakePointRepository) pointRepository).points.remove(userIdObj);

            // Act & Assert (실행 및 검증)
            CoreException exception = assertThrows(CoreException.class, () ->
                    orderService.createOrder(nonExistentUserId, items)
            );
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
