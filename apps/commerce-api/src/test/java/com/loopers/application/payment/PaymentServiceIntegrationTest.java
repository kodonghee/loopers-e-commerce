package com.loopers.application.payment;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderResult;
import com.loopers.application.order.OrderService;
import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.order.PaymentMethod;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.payment.pg.PgClient;
import com.loopers.infrastructure.payment.pg.PgDto;
import com.loopers.utils.DatabaseCleanUp;
import feign.FeignException;
import feign.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@Slf4j
@SpringBootTest
class PaymentServiceIntegrationTest {

    @Autowired
    private ProductFacade productFacade;
    @Autowired private PaymentService paymentService;
    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private BrandJpaRepository brandJpaRepository;
    @Autowired private DatabaseCleanUp databaseCleanUp;

    @MockitoBean
    private PgClient pgClient;

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
                order.orderId(),
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
        given(pgClient.requestPayment(anyString(), any()))
                .willReturn(new PgDto.ResponseWrapper(
                        new PgDto.ResponseWrapper.Meta("SUCCESS", null, null),
                        new PgDto.ResponseWrapper.Data("tx-123", "PENDING")
                ));

        // Act
        PaymentResult result = paymentService.requestCardPayment(criteria);

        // Assert
        assertThat(result.orderId()).isEqualTo(criteria.orderId());
        assertThat(result.status()).isEqualTo(PaymentStatus.PENDING); // 아직 콜백 전이므로 PENDING
        assertThat(result.paymentId()).isEqualTo("tx-123");
    }

    @Test
    @DisplayName("잘못된 카드 번호면 DECLINED 상태로 처리된다")
    void requestCardPayment_invalidCard_declined() {
        // Arrange
        PaymentCriteria criteria = createCardPaymentCriteria()
                .withCardNo("0000-0000-0000-0000");

        Request fakeRequest = Request.create(
                Request.HttpMethod.POST,
                "/api/v1/payments",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null
        );

        byte[] emptyBody = new byte[0];

        FeignException.BadRequest badRequestEx =
                new FeignException.BadRequest("잘못된 카드", fakeRequest, emptyBody, Collections.emptyMap());

        given(pgClient.requestPayment(anyString(), any()))
                .willThrow(badRequestEx);

        // Act & Assert
        assertThatThrownBy(() -> paymentService.requestCardPayment(criteria))
                .isInstanceOf(FeignException.BadRequest.class);

        var order = orderService.getOrderDetail(criteria.orderId());
        assertThat(order.status()).isEqualTo(OrderStatus.PAYMENT_DECLINED);
    }

    @Test
    @DisplayName("PG 장애 발생 시 CircuitBreaker Fallback으로 ERROR 상태가 된다")
    void requestCardPayment_fallback_error() {
        // Arrange
        PaymentCriteria criteria = createCardPaymentCriteria();
        given(pgClient.requestPayment(anyString(), any()))
                .willThrow(new RuntimeException("PG 장애"));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.requestCardPayment(criteria))
                .isInstanceOf(RuntimeException.class);

        var order = orderService.getOrderDetail(criteria.orderId());
        assertThat(order.status()).isEqualTo(OrderStatus.PAYMENT_ERROR);
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
                criteria.couponId(),
                "정상 승인되었습니다."
        );

        // Act
        paymentService.handlePgCallback(callback);

        // Assert
        var order = orderService.getOrderDetail(criteria.orderId());
        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("PG 콜백 금액이 다르면 DECLINED 처리된다")
    void handlePgCallback_invalidAmount() {
        // Arrange
        PaymentCriteria criteria = createCardPaymentCriteria();
        PaymentCallback callback = new PaymentCallback(
                criteria.orderId(),
                "pgPaymentId-123",
                "SUCCESS",
                criteria.userId(),
                criteria.amount().add(BigDecimal.TEN),
                criteria.couponId(),
                "금액 불일치"
        );

        // Act & Assert
        assertThatThrownBy(() -> paymentService.handlePgCallback(callback))
                .isInstanceOf(IllegalStateException.class);

        var order = orderService.getOrderDetail(criteria.orderId());
        assertThat(order.status()).isEqualTo(OrderStatus.PAYMENT_DECLINED);
    }

    @Test
    @DisplayName("콜백이 누락된 경우에도 상태 조회 API로 확인할 수 있다")
    void checkPaymentStatus_whenCallbackMissing() {
        // Arrange
        PaymentCriteria criteria = createCardPaymentCriteria();

        given(pgClient.requestPayment(anyString(), any()))
                .willReturn(new PgDto.ResponseWrapper(
                        new PgDto.ResponseWrapper.Meta("SUCCESS", null, null),
                        new PgDto.ResponseWrapper.Data("tx-123", "PENDING")
                ));

        paymentService.requestCardPayment(criteria);

        given(pgClient.findByOrderId(anyString(), anyString()))
                .willReturn(new PgDto.ResponseWrapper(
                        new PgDto.ResponseWrapper.Meta("SUCCESS", null, null),
                        new PgDto.ResponseWrapper.Data("tx-123", "SUCCESS")
                ));

        // Act
        PaymentResult result = paymentService.findByOrderId(criteria.userId(), criteria.orderId());

        // Assert
        assertThat(result.orderId()).isEqualTo(criteria.orderId());
        assertThat(result.status()).isEqualTo("SUCCESS");
    }
}
