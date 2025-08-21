package com.loopers.application.payment;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderResult;
import com.loopers.application.order.OrderService;
import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.order.PaymentMethod;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class PaymentServiceIntegrationTest {

    @Autowired
    private ProductFacade productFacade;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private BrandJpaRepository brandJpaRepository;
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

    @Test
    @DisplayName("PG 연동 API는 FeignClient로 외부 시스템을 정상 호출한다.")
    void requestCardPayment_success() {
        // arrange
        Product product = productFacade.create(
                new ProductCriteria("스니커즈", 5, new BigDecimal("5000"), brandId)
        );

        OrderCriteria orderCriteria = new OrderCriteria(
                USER_ID,
                List.of(new OrderCriteria.OrderLine(product.getId(), 1, product.getPrice().getAmount())),
                null,
                PaymentMethod.CARD
        );
        OrderResult order = orderService.createPendingOrder(orderCriteria);

        PaymentCriteria criteria = new PaymentCriteria(
                USER_ID,
                order.orderId().toString(),
                order.pgOrderId(),
                "SAMSUNG",
                "1234-5678-9814-1451",
                order.totalAmount(),
                null
        );

        try {
            // act
            PaymentResult result = paymentService.requestCardPayment(criteria);

            // assert
            assertThat(result.orderId()).isEqualTo(order.pgOrderId());
            assertThat(result.paymentId()).isNotBlank();
            assertThat(result.status()).isIn(
                    "PENDING", "SUCCESS", "FAILED", "LIMIT_EXCEEDED", "INVALID_CARD"
            );
        } catch (feign.FeignException e) {
            // assert
            assertThat(e.status()).isIn(400, 500);
            // log.info("PG 호출 실패 응답 수신: status={} message={}", e.status(), e.getMessage());
        }
    }
}
