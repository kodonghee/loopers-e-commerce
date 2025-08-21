package com.loopers.application.order;

import com.loopers.application.coupon.CouponCriteria;
import com.loopers.application.coupon.CouponUseCase;
import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentService;
import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.order.PaymentMethod;
import com.loopers.domain.point.Point;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.coupon.CouponJpaRepository;
import com.loopers.infrastructure.order.OrderJpaRepository;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("주문 동시성 테스트")
class OrderServiceConcurrencyIntegrationTest {

    @Autowired
    private CouponUseCase couponUseCase;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private ProductFacade productFacade;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PointJpaRepository pointJpaRepository;
    @Autowired
    private OrderJpaRepository orderJpaRepository;
    @Autowired
    private BrandJpaRepository brandJpaRepository;
    @Autowired
    private ProductJpaRepository productJpaRepository;
    @Autowired
    private CouponJpaRepository couponJpaRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String USER_ID = "Annie";
    private Long brandId;

    @BeforeEach
    void setUp() {
        User user = new User(USER_ID, Gender.F, "1995-06-11", "test@naver.com");
        userRepository.save(user);

        pointJpaRepository.save(new Point(USER_ID, new BigDecimal("300000")));

        Brand brand = brandJpaRepository.save(new Brand("Nike"));
        this.brandId = brand.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용되어야 한다.")
    @Test
    void shouldCouponUsedOnce_whenConcurrentRequests() throws InterruptedException {
        Product product = productFacade.create(
                new ProductCriteria("스니커즈", 10, new BigDecimal("100000"), brandId)
        );
        Long userCouponId = couponUseCase.createCoupon(new CouponCriteria(USER_ID, CouponType.FIXED, new BigDecimal("1000")));

        OrderCriteria orderCriteria = new OrderCriteria(
                USER_ID,
                List.of(new OrderCriteria.OrderLine(product.getId(), 1, product.getPrice().getAmount())),
                userCouponId,
                PaymentMethod.POINTS
        );

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderResult order = orderService.createPendingOrder(orderCriteria);
                    orderService.confirmPayment(order.orderId(), userCouponId);
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Long successOrders = orderJpaRepository.count();
        System.out.println("생성된 주문 수 = " + successOrders);

        var coupon = couponJpaRepository.findById(userCouponId)
                .orElseThrow();
        assertThat(coupon.isUsed()).isTrue();
    }

    @DisplayName("동일한 유저가 서로 다른 주문을 동시에 수행해도, 포인트가 정상적으로 차감되어야 한다.")
    @Test
    void shouldDecreasePointCorrectly_whenConcurrentRequests() throws Exception {
        Product p1 = productFacade.create(new ProductCriteria("A", 10, new BigDecimal("100000"), brandId));
        Product p2 = productFacade.create(new ProductCriteria("B", 10, new BigDecimal("100000"), brandId));

        List<OrderCriteria> payloads = List.of(
                new OrderCriteria(USER_ID, List.of(new OrderCriteria.OrderLine(p1.getId(), 1, p1.getPrice().getAmount())), null, PaymentMethod.POINTS),
                new OrderCriteria(USER_ID, List.of(new OrderCriteria.OrderLine(p2.getId(), 1, p2.getPrice().getAmount())), null, PaymentMethod.POINTS)
        );

        int threadCount = 100;
        ExecutorService es = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            OrderCriteria c = payloads.get(i % payloads.size());
            es.submit(() -> {
                try {
                    OrderResult order= orderService.createPendingOrder(c);

                    PaymentCriteria paymentCriteria = new PaymentCriteria(
                            USER_ID,
                            order.orderId().toString(),
                            order.pgOrderId(),
                            null,
                            null,
                            order.totalAmount(),
                            null
                    );
                    paymentService.processPointPayment(paymentCriteria);
                } catch (Exception ignored) {}
                finally { latch.countDown(); }
            });
        }

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertThat(completed).as("모든 스레드가 제한 시간 내 실행을 완료해야 함").isTrue();

        Long successOrders = orderJpaRepository.countByStatus(OrderStatus.PAID);

        BigDecimal expectedRemain = new BigDecimal("300000")
                .subtract(new BigDecimal("100000").multiply(new BigDecimal(successOrders)));

        BigDecimal actualRemain = pointJpaRepository.findByUserId(USER_ID).orElseThrow().getPointValue();
        assertThat(actualRemain).isEqualByComparingTo(expectedRemain);
    }

    @DisplayName("동일한 상품에 대해 여러 주문이 동시에 요청되어도, 재고가 정상적으로 차감되어야 한다.")
    @Test
    void shouldDecreaseStockCorrectly_whenConcurrentRequests() throws Exception {
        int initialStock = 30;
        BigDecimal price = new BigDecimal("100000");

        Product product = productFacade.create(
                new ProductCriteria("스니커즈", initialStock, price, brandId)
        );

        int threadCount = 50;
        OrderCriteria payload = new OrderCriteria(
                USER_ID,
                List.of(new OrderCriteria.OrderLine(product.getId(), 1, product.getPrice().getAmount())),
                null,
                PaymentMethod.POINTS
        );

        ExecutorService es = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            es.submit(() -> {
                try {
                    var order = orderService.createPendingOrder(payload);
                    orderService.confirmPayment(order.orderId(), null);
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(15, TimeUnit.SECONDS);
        assertThat(completed).as("쓰레드 작업이 제한 시간 내 완료 되어야 함").isTrue();

        long successOrders = orderJpaRepository.countByStatus(OrderStatus.PAID);

        int expectedRemain = initialStock - (int) successOrders;
        int actualRemain = productJpaRepository.findById(product.getId())
                .orElseThrow()
                .getStock()
                .getValue();

        assertThat(actualRemain).isEqualTo(expectedRemain);
        assertThat(actualRemain).isGreaterThanOrEqualTo(0);
    }

}
