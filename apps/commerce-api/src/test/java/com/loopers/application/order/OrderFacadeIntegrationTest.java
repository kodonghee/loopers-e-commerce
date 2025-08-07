package com.loopers.application.order;

import com.loopers.application.coupon.CouponCriteria;
import com.loopers.application.coupon.CouponUseCase;
import com.loopers.application.order.port.OrderEventSender;
import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductFacade productFacade;
    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BrandJpaRepository brandJpaRepository;
    @Autowired
    private CouponUseCase couponUseCase;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private OrderRepository orderRepository;
    @MockitoSpyBean
    private OrderEventSender orderEventSender;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String USER_ID = "cookie95";
    private Long brandId;

    @BeforeEach
    void setUp() {
        User user = new User(USER_ID, Gender.F, "1995-06-11", "test@naver.com");
        userRepository.save(user);

        pointRepository.save(new Point(USER_ID, new BigDecimal("300000")));

        Brand brand = brandJpaRepository.save(new Brand("Paris Baguette"));
        this.brandId = brand.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문 생성 통합 테스트")
    @Nested
    class PlaceOrderTest {
        @DisplayName("정상적인 주문 생성 요청 시, 주문을 성공적으로 처리한다.")
        @Test
        @Transactional
        void placeOrderSuccessfully_whenAllConditionsMet() {
            // arrange
            Product product = productFacade.create(
                    new ProductCriteria("스니커즈", 10, new BigDecimal("100000"), brandId)
            );
            Long userCouponId = couponUseCase.createCoupon(new CouponCriteria(USER_ID, CouponType.FIXED, new BigDecimal("50000")));

            OrderCriteria criteria = new OrderCriteria(
                    USER_ID,
                    List.of(new OrderCriteria.OrderLine(product.getId(), 2, product.getPrice().getAmount())),
                    userCouponId
            );

            // act
            OrderResult orderResult = orderFacade.placeOrder(criteria);

            // assert
            assertThat(orderResult.totalAmount()).isEqualTo(new BigDecimal("200000"));
            assertThat(orderRepository.findById(orderResult.orderId())).isPresent();
            assertThat(couponRepository.findById(userCouponId).get().isUsed()).isTrue();
            assertThat(productRepository.findById(product.getId()).get().getStock().getValue()).isEqualTo(8);
            assertThat(pointRepository.find(new UserId(USER_ID)).get().getPointValue())
                    .isEqualByComparingTo(new BigDecimal("150000"));
        }

        @DisplayName("재고 부족으로 주문 실패 시, 포인트 차감이나 쿠폰 사용이 없어야 한다.")
        @Test
        void shouldRollbackAll_whenStockIsInsufficient() {
            // arrange
            Product product = productFacade.create(
                    new ProductCriteria("스니커즈", 1, new BigDecimal("100000"), brandId)
            );
            Long userCouponId = couponUseCase.createCoupon(new CouponCriteria(USER_ID, CouponType.FIXED, new BigDecimal("50000")));

            OrderCriteria criteria = new OrderCriteria(
                    USER_ID,
                    List.of(new OrderCriteria.OrderLine(product.getId(), 2, product.getPrice().getAmount())),
                    userCouponId
            );

            // act
            Throwable thrown = catchThrowable(() -> orderFacade.placeOrder(criteria));

            // assert
            assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
            assertThat(couponRepository.findById(userCouponId).get().isUsed()).isFalse();
            assertThat(productRepository.findById(product.getId()).get().getStock().getValue()).isEqualTo(1);
            assertThat(pointRepository.find(new UserId(USER_ID)).get().getPointValue())
                    .isEqualByComparingTo(new BigDecimal("300000"));
            assertThat(orderRepository.findAllByUserId(new UserId(USER_ID))).isEmpty();
        }

        @DisplayName("포인트 부족으로 주문 실패 시, 재고 차감이나 쿠폰 사용이 없어야 한다.")
        @Test
        void shouldRollbackAll_whenPointIsInsufficient() {
            // arrange
            Product product = productFacade.create(
                    new ProductCriteria("스니커즈", 10, new BigDecimal("200000"), brandId)
            );
            Long userCouponId = couponUseCase.createCoupon(new CouponCriteria(USER_ID, CouponType.FIXED, new BigDecimal("50000")));

            OrderCriteria criteria = new OrderCriteria(
                    USER_ID,
                    List.of(new OrderCriteria.OrderLine(product.getId(), 2, product.getPrice().getAmount())),
                    userCouponId
            );

            // act
            Throwable thrown = catchThrowable(() -> orderFacade.placeOrder(criteria));

            // assert
            assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
            assertThat(couponRepository.findById(userCouponId).get().isUsed()).isFalse();
            assertThat(productRepository.findById(product.getId()).get().getStock().getValue()).isEqualTo(10);
            assertThat(pointRepository.find(new UserId(USER_ID)).get().getPointValue())
                    .isEqualByComparingTo(new BigDecimal("300000"));
            assertThat(orderRepository.findAllByUserId(new UserId(USER_ID))).isEmpty();
        }

        @DisplayName("잘못된 쿠폰 사용 시, 전체 주문이 실패한다.")
        @Test
        void shouldRollbackAll_whenCouponInvalid() {
            // arrange
            Product product = productFacade.create(
                    new ProductCriteria("스니커즈", 5, new BigDecimal("100000"), brandId)
            );

            String otherUserId = "newjeans95";
            userRepository.save(new User(otherUserId, Gender.M, "1990-10-31", "other@naver.com"));
            pointRepository.save(new Point(otherUserId, new BigDecimal("300000")));
            Long otherUserCouponId = couponUseCase.createCoupon(
                    new CouponCriteria(otherUserId, CouponType.FIXED, new BigDecimal("50000"))
            );

            OrderCriteria criteria = new OrderCriteria(
                    USER_ID,
                    List.of(new OrderCriteria.OrderLine(product.getId(), 1, product.getPrice().getAmount())),
                    otherUserCouponId
            );

            // act
            Throwable thrown = catchThrowable(() -> orderFacade.placeOrder(criteria));

            // assert
            assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
            assertThat(couponRepository.findById(otherUserCouponId).get().isUsed()).isFalse();
            assertThat(productRepository.findById(product.getId()).get().getStock().getValue()).isEqualTo(5);
            assertThat(pointRepository.find(new UserId(USER_ID)).get().getPointValue())
                    .isEqualByComparingTo(new BigDecimal("300000"));
            assertThat(orderRepository.findAllByUserId(new UserId(USER_ID))).isEmpty();
        }

        @DisplayName("존재하지 않는 쿠폰 사용 시, 전체 주문이 실패한다.")
        @Test
        void shouldRollbackAll_whenCouponDoesNotExist() {
            // arrange
            Product product = productFacade.create(
                    new ProductCriteria("스니커즈", 5, new BigDecimal("100000"), brandId)
            );

            Long invalidCouponId = 9999L;

            OrderCriteria criteria = new OrderCriteria(
                    USER_ID,
                    List.of(new OrderCriteria.OrderLine(product.getId(), 1, product.getPrice().getAmount())),
                    invalidCouponId
            );

            // act
            Throwable thrown = catchThrowable(() -> orderFacade.placeOrder(criteria));

            // assert
            assertThat(thrown).isInstanceOf(CoreException.class);
            assertThat(productRepository.findById(product.getId()).get().getStock().getValue()).isEqualTo(5);
            assertThat(pointRepository.find(new UserId(USER_ID)).get().getPointValue())
                    .isEqualByComparingTo(new BigDecimal("300000"));
            assertThat(orderRepository.findAllByUserId(new UserId(USER_ID))).isEmpty();
        }

    }

   /* @DisplayName("getOrderList 메서드 테스트")
    @Nested
    class GetOrderListTest {
        @DisplayName("존재하는 유저의 주문 목록을 조회할 경우, 주문 정보 리스트를 반환한다.")
        @Test
        void getOrderList_shouldReturnOrderInfoList() {
            // arrange
            UserId userId = new UserId(USER_ID);
            Order mockOrder1 = mock(Order.class);
            when(mockOrder1.getId()).thenReturn(101L);
            when(mockOrder1.getUserId()).thenReturn(USER_ID);
            when(mockOrder1.getTotalAmount()).thenReturn(new BigDecimal("10000"));
            when(mockOrder1.getOrderItems()).thenReturn(List.of(new com.loopers.domain.order.OrderItem(1L, 1, new BigDecimal("10000"))));

            Order mockOrder2 = mock(Order.class);
            when(mockOrder2.getId()).thenReturn(102L);
            when(mockOrder2.getUserId()).thenReturn(USER_ID);
            when(mockOrder2.getTotalAmount()).thenReturn(new BigDecimal("5000"));
            when(mockOrder2.getOrderItems()).thenReturn(List.of(new com.loopers.domain.order.OrderItem(2L, 1, new BigDecimal("5000"))));

            when(orderRepository.findAllByUserId(userId)).thenReturn(List.of(mockOrder1, mockOrder2));

            // act
            List<OrderResult> result = orderFacade.getOrderList(userId);

            // assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).orderId()).isEqualTo(101L);
            assertThat(result.get(0).totalAmount()).isEqualByComparingTo(new BigDecimal("10000"));
            assertThat(result.get(1).orderId()).isEqualTo(102L);
            assertThat(result.get(1).totalAmount()).isEqualByComparingTo(new BigDecimal("5000"));
        }
    }

    @DisplayName("getOrderDetail 메서드 테스트")
    @Nested
    class GetOrderDetailTest {
        @DisplayName("존재하는 주문 ID로 상세 정보를 조회할 경우, 주문 정보를 반환한다.")
        @Test
        void getOrderDetail_shouldReturnOrderInfo() {
            // arrange
            Order mockOrder = mock(Order.class);
            when(mockOrder.getId()).thenReturn(ORDER_ID);
            when(mockOrder.getUserId()).thenReturn(USER_ID);
            when(mockOrder.getTotalAmount()).thenReturn(new BigDecimal("2500"));
            when(mockOrder.getOrderItems()).thenReturn(
                    List.of(new com.loopers.domain.order.OrderItem(1L, 2, new BigDecimal("1000")),
                            new com.loopers.domain.order.OrderItem(2L, 1, new BigDecimal("500")))
            );
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(mockOrder));

            // act
            OrderResult result = orderFacade.getOrderDetail(ORDER_ID);

            // assert
            assertThat(result.orderId()).isEqualTo(ORDER_ID);
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("2500"));
            assertThat(result.items()).hasSize(2);
        }

        @DisplayName("존재하지 않는 주문 ID로 조회할 경우, 예외를 발생시킨다.")
        @Test
        void getOrderDetail_shouldThrowException_whenOrderNotFound() {
            // arrange
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

            // act & assert
            assertThrows(IllegalArgumentException.class, () -> orderFacade.getOrderDetail(ORDER_ID));
        }
    }*/
}
