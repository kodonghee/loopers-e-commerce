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
import com.loopers.domain.order.PaymentMethod;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;
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

        Brand brand = brandJpaRepository.save(new Brand("Nike"));
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
        void placeOrderSuccessfully_whenAllConditionsMet() {
            // arrange
            Product product = productFacade.create(
                    new ProductCriteria("스니커즈", 10, new BigDecimal("100000"), brandId)
            );
            Long userCouponId = couponUseCase.createCoupon(new CouponCriteria(USER_ID, CouponType.FIXED, new BigDecimal("50000")));

            OrderCriteria criteria = new OrderCriteria(
                    USER_ID,
                    List.of(new OrderCriteria.OrderLine(product.getId(), 2, product.getPrice().getAmount())),
                    userCouponId,
                    PaymentMethod.POINTS
            );

            // act
            OrderResult orderResult = orderService.placeOrder(criteria);

            // assert
            assertThat(orderResult.totalAmount()).isEqualTo(new BigDecimal("200000"));
            assertThat(orderRepository.findById(orderResult.orderId())).isPresent();
            assertThat(couponRepository.findById(userCouponId).get().isUsed()).isTrue();
            assertThat(productRepository.findById(product.getId()).get().getStock().getValue()).isEqualTo(8);
            assertThat(pointRepository.find(new UserId(USER_ID)).get().getPointValue())
                    .isEqualByComparingTo(new BigDecimal("150000"));
        }

        @DisplayName("존재하지 않는 상품으로 주문 시도 시, 전체 주문이 실패한다.")
        @Test
        void shouldRollback_whenProductDoesNotExist() {
            Long invalidProductId = 99999L;

            OrderCriteria criteria = new OrderCriteria(
                    USER_ID,
                    List.of(new OrderCriteria.OrderLine(invalidProductId, 1, new BigDecimal("100000"))),
                    null,
                    PaymentMethod.POINTS
            );

            Throwable thrown = catchThrowable(() -> orderService.placeOrder(criteria));

            assertThat(thrown).isInstanceOf(CoreException.class);
            assertThat(pointRepository.find(new UserId(USER_ID)).get().getPointValue())
                    .isEqualByComparingTo(new BigDecimal("300000"));
            assertThat(orderRepository.findAllByUserId(new UserId(USER_ID))).isEmpty();
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
                    userCouponId,
                    PaymentMethod.POINTS
            );

            // act
            Throwable thrown = catchThrowable(() -> orderService.placeOrder(criteria));

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
                    userCouponId,
                    PaymentMethod.POINTS
            );

            // act
            Throwable thrown = catchThrowable(() -> orderService.placeOrder(criteria));

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
                    otherUserCouponId,
                    PaymentMethod.POINTS
            );

            // act
            Throwable thrown = catchThrowable(() -> orderService.placeOrder(criteria));

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
                    invalidCouponId,
                    PaymentMethod.POINTS
            );

            // act
            Throwable thrown = catchThrowable(() -> orderService.placeOrder(criteria));

            // assert
            assertThat(thrown).isInstanceOf(CoreException.class);
            assertThat(productRepository.findById(product.getId()).get().getStock().getValue()).isEqualTo(5);
            assertThat(pointRepository.find(new UserId(USER_ID)).get().getPointValue())
                    .isEqualByComparingTo(new BigDecimal("300000"));
            assertThat(orderRepository.findAllByUserId(new UserId(USER_ID))).isEmpty();
        }

        @DisplayName("이미 사용한 쿠폰을 사용할 경우, 전체 주문이 실패한다.")
        @Test
        void shouldRollbackAll_whenCouponAlreadyUsed() {
            // arrange
            Product product = productFacade.create(
                    new ProductCriteria("후드티", 5, new BigDecimal("100000"), brandId)
            );
            Long userCouponId = couponUseCase.createCoupon(
                    new CouponCriteria(USER_ID, CouponType.FIXED, new BigDecimal("50000"))
            );

            OrderCriteria firstOrder = new OrderCriteria(
                    USER_ID,
                    List.of(new OrderCriteria.OrderLine(product.getId(), 1, product.getPrice().getAmount())),
                    userCouponId,
                    PaymentMethod.POINTS
            );
            orderService.placeOrder(firstOrder);

            OrderCriteria secondOrder = new OrderCriteria(
                    USER_ID,
                    List.of(new OrderCriteria.OrderLine(product.getId(), 1, product.getPrice().getAmount())),
                    userCouponId,
                    PaymentMethod.POINTS
            );

            // act
            Throwable thrown = catchThrowable(() -> orderService.placeOrder(secondOrder));

            // assert
            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            assertThat(couponRepository.findById(userCouponId).get().isUsed()).isTrue();
            assertThat(productRepository.findById(product.getId()).get().getStock().getValue()).isEqualTo(4);
            assertThat(pointRepository.find(new UserId(USER_ID)).get().getPointValue())
                    .isEqualByComparingTo(new BigDecimal("250000"));
            assertThat(orderRepository.findAllByUserId(new UserId(USER_ID)).size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("주문 조회 통합 테스트")
    class GetOrderTest {

        @Test
        @DisplayName("사용자 ID로 주문 목록을 조회할 수 있다.")
        void getOrderList_byUserId() {
            // arrange
            Product product = productFacade.create(
                    new ProductCriteria("운동화", 3, new BigDecimal("120000"), brandId)
            );

            OrderCriteria criteria = new OrderCriteria(
                    USER_ID,
                    List.of(new OrderCriteria.OrderLine(product.getId(), 1, product.getPrice().getAmount())),
                    null,
                    PaymentMethod.POINTS
            );
            OrderResult createdOrder = orderService.placeOrder(criteria);

            // act
            List<OrderResult> resultList = orderService.getOrderList(new UserId(USER_ID));

            // assert
            assertThat(resultList).hasSize(1);

            OrderResult result = resultList.get(0);
            assertThat(result.orderId()).isEqualTo(createdOrder.orderId());
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("120000"));
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).productId()).isEqualTo(product.getId());
            assertThat(result.items().get(0).quantity()).isEqualTo(1);
            assertThat(result.items().get(0).price()).isEqualByComparingTo(new BigDecimal("120000"));
        }

        @Test
        @DisplayName("주문 ID로 주문 상세를 조회할 수 있다.")
        void getOrderDetail_byOrderId() {
            // arrange
            Product product = productFacade.create(
                    new ProductCriteria("런닝화", 5, new BigDecimal("90000"), brandId)
            );

            OrderCriteria criteria = new OrderCriteria(
                    USER_ID,
                    List.of(new OrderCriteria.OrderLine(product.getId(), 2, product.getPrice().getAmount())),
                    null,
                    PaymentMethod.POINTS
            );
            OrderResult createdOrder = orderService.placeOrder(criteria);

            // act
            OrderResult result = orderService.getOrderDetail(createdOrder.orderId());

            // assert
            assertThat(result.orderId()).isEqualTo(createdOrder.orderId());
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("180000"));
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).productId()).isEqualTo(product.getId());
            assertThat(result.items().get(0).quantity()).isEqualTo(2);
            assertThat(result.items().get(0).price()).isEqualByComparingTo(new BigDecimal("90000"));
        }
    }
}
