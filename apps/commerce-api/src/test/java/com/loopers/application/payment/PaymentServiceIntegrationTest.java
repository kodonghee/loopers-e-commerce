package com.loopers.application.payment;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderResult;
import com.loopers.application.order.OrderService;
import com.loopers.application.payment.port.PaymentGateway;
import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.order.OrderStatus;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
class PaymentServiceIntegrationTest {

    @Autowired private ProductFacade productFacade;
    @Autowired private PaymentService paymentService;
    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private BrandJpaRepository brandJpaRepository;
    @Autowired private DatabaseCleanUp databaseCleanUp;
    @MockitoBean
    private PaymentGateway gateway;

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

    private PaymentCriteria createCardPaymentCriteria() {
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

        return new PaymentCriteria(
                USER_ID,
                order.orderId().toString(),
                order.pgOrderId(),
                "SAMSUNG",
                "1234-5678-9814-1451",
                order.totalAmount(),
                null
        );
    }

    @Test
    @DisplayName("PG 연동 API를 통해 카드 결제를 정상적으로 요청한다")
    void requestCardPayment_success() {
        // Arrange
        PaymentCriteria criteria = createCardPaymentCriteria();

        // Act
        PaymentResult result = paymentService.requestCardPayment(criteria);

        // Assert
        assertThat(result.orderId()).isEqualTo(criteria.pgOrderId());
        assertThat(result.status()).isIn("PENDING", "SUCCESS", "FAILED", "LIMIT_EXCEEDED", "INVALID_CARD");

        if ("PENDING".equals(result.status())) {
            assertThat(result.paymentId()).isNull();
        } else {
            assertThat(result.paymentId()).isNotBlank();
        }
    }

    @Test
    @DisplayName("PG 연동 API 호출 시 잘못된 카드 번호면 PENDING 상태를 먼저 반환한다")
    void requestCardPayment_invalidCard_pending() {
        // Arrange
        PaymentCriteria criteria = createCardPaymentCriteria()
                .withCardNo("0000-0000-0000-0000");;

        // Act
        PaymentResult result = paymentService.requestCardPayment(criteria);

        // Assert
        assertThat(result.status()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("PG 연동 장애 발생 시 CircuitBreaker Fallback으로 PENDING 상태를 반환한다")
    void requestCardPayment_fallback() {
        // Arrange
        PaymentCriteria criteria = createCardPaymentCriteria();
        when(gateway.request(any())).thenThrow(new RuntimeException("PG 장애"));

        // Act
        PaymentResult result = paymentService.requestCardPayment(criteria);

        // Assert
        assertThat(result.orderId()).isEqualTo(criteria.pgOrderId());
        assertThat(result.paymentId()).isNull();
        assertThat(result.status()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("PG 콜백이 성공 상태면 주문이 결제 완료된다")
    void handlePgCallback_success() {
        // Arrange
        PaymentCriteria criteria = createCardPaymentCriteria();
        PaymentCallback callback = new PaymentCallback(
                criteria.orderId(),
                "pgPaymentId-123",
                "SUCCESS",
                criteria.userId(),
                criteria.amount(),
                criteria.couponId()
        );

        // Act
        paymentService.handlePgCallback(callback);

        // Assert
        var order = orderService.getOrderDetail(Long.valueOf(criteria.orderId()));
        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("PG 콜백 금액이 다르면 결제 실패 처리된다")
    void handlePgCallback_invalidAmount() {
        // Arrange
        PaymentCriteria criteria = createCardPaymentCriteria();
        PaymentCallback callback = new PaymentCallback(
                criteria.orderId(),
                "pgPaymentId-123",
                "SUCCESS",
                criteria.userId(),
                criteria.amount().add(BigDecimal.TEN),
                criteria.couponId()
        );

        // Act & Assert
        assertThatThrownBy(() -> paymentService.handlePgCallback(callback))
                .isInstanceOf(IllegalStateException.class);

        var order = orderService.getOrderDetail(Long.valueOf(criteria.orderId()));
        assertThat(order.status()).isEqualTo(OrderStatus.PAYMENT_FAILED);
    }

    @Test
    @DisplayName("콜백이 오지 않아도 상태 확인 API를 통해 결제 상태를 복구할 수 있다")
    void recoverPaymentStatus_whenCallbackMissing() {
        // Arrange
        PaymentCriteria criteria = createCardPaymentCriteria();
        paymentService.requestCardPayment(criteria);

        // Act
        PaymentResult recovered = paymentService.findByOrderId(criteria.userId(), criteria.orderId());

        // Assert
        assertThat(recovered.orderId()).isEqualTo(criteria.orderId());
        assertThat(recovered.status()).isIn("PENDING", "SUCCESS", "FAILED", "LIMIT_EXCEEDED", "INVALID_CARD");
    }
}
