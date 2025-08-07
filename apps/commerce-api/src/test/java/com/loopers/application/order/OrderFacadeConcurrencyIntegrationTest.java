package com.loopers.application.order;

import com.loopers.application.coupon.CouponCriteria;
import com.loopers.application.coupon.CouponUseCase;
import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.order.OrderJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("주문 동시성 테스트")
class OrderFacadeConcurrencyIntegrationTest {

    @Autowired
    private CouponUseCase couponUseCase;
    @Autowired
    private OrderFacade orderFacade;
    @Autowired
    private ProductFacade productFacade;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private OrderJpaRepository orderJpaRepository;
    @Autowired
    private BrandJpaRepository brandJpaRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String USER_ID = "Annie";
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
                userCouponId
        );

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderFacade.placeOrder(orderCriteria);
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Long successCount = orderJpaRepository.count();
        assertThat(successCount).isEqualTo(1);
    }

    @DisplayName("동일한 유저가 서로 다른 주문을 동시에 수행해도, 포인트가 정상적으로 차감되어야 한다.")
    @Test
    void likeDecreaseCorrectly_whenConcurrentRequests() throws Exception {

    }

    @DisplayName("좋아요와 싫어요가 섞여도 최종 좋아요 수가 정확하다.")
    @Test
    void mixedLikeAndCancel() throws Exception {

    }

}
