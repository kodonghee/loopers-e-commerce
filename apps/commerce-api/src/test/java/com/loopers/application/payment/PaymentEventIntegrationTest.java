package com.loopers.application.payment;

import com.loopers.application.coupon.CouponCriteria;
import com.loopers.application.coupon.CouponEventHandler;
import com.loopers.application.coupon.CouponUseCase;
import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderResult;
import com.loopers.application.order.OrderService;
import com.loopers.application.payment.port.PaymentGateway;
import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.PaymentMethod;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class PaymentEventIntegrationTest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired private ProductFacade productFacade;
    @Autowired private CouponUseCase couponUseCase;
    @Autowired private UserRepository userRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private BrandJpaRepository brandJpaRepository;
    @Autowired private DatabaseCleanUp databaseCleanUp;

    @MockitoSpyBean
    private CouponEventHandler couponEventHandler;
    @MockitoBean
    private PaymentGateway gateway;

    private static final String USER_ID = "cookie95";
    private Long brandId;

    @BeforeEach
    void setUp() {
        userRepository.save(new User(USER_ID, Gender.F, "1995-06-11", "test@naver.com"));
        pointRepository.save(new com.loopers.domain.point.Point(USER_ID, new BigDecimal("300000")));

        Brand brand = brandJpaRepository.save(new Brand("Nike"));
        this.brandId = brand.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트 결제 성공 시 PaymentCompletedEvent 가 발행되고 쿠폰이 사용 처리 된다.")
    @Test
    void shouldPublishEventAndMarkCoupon_whenPointPaymentSuccess() {
        // arrange
        var product = productFacade.create(
                new ProductCriteria("스니커즈", 5, new BigDecimal("100000"), brandId)
        );
        Long couponId = couponUseCase.createCoupon(
                new CouponCriteria(USER_ID, CouponType.FIXED, new BigDecimal("50000"))
        );

        OrderCriteria orderCriteria = new OrderCriteria(
                USER_ID,
                List.of(new OrderCriteria.OrderLine(product.getId(), 1, product.getPrice().getAmount())),
                couponId,
                PaymentMethod.POINTS
        );
        OrderResult orderResult = orderService.createPendingOrder(orderCriteria);

        PaymentCriteria paymentCriteria = new PaymentCriteria(
                USER_ID,
                orderResult.orderId(),
                null,
                null,
                orderResult.totalAmount(),
                couponId
        );

        // act
        paymentService.processPointPayment(paymentCriteria);

        // assert
        assertThat(couponRepository.findById(couponId).get().isUsed()).isTrue();
        assertThat(orderRepository.findByOrderId(orderResult.orderId()).get().isPaid()).isTrue();

        verify(couponEventHandler, timeout(1000)).handle(
                org.mockito.ArgumentMatchers.any()
        );
    }

    @DisplayName("카드 결제 콜백 성공 시 PaymentCompletedEvent 가 발행되고 쿠폰이 사용 처리 된다.")
    @Test
    void shouldPublishEventAndMarkCoupon_whenCardPaymentCallbackSuccess() {
        // arrange
        var product = productFacade.create(
                new ProductCriteria("운동화", 5, new BigDecimal("150000"), brandId)
        );
        Long couponId = couponUseCase.createCoupon(
                new CouponCriteria(USER_ID, CouponType.FIXED, new BigDecimal("50000"))
        );

        OrderCriteria orderCriteria = new OrderCriteria(
                USER_ID,
                List.of(new OrderCriteria.OrderLine(product.getId(), 1, product.getPrice().getAmount())),
                couponId,
                PaymentMethod.CARD
        );
        OrderResult orderResult = orderService.createPendingOrder(orderCriteria);

        when(gateway.request(any()))
                .thenReturn(new PaymentGateway.Response(orderResult.orderId(), "ext-123", "SUCCESS", null));

        // 카드 결제 요청 → pending 상태
        paymentService.requestCardPayment(
                new PaymentCriteria(USER_ID, orderResult.orderId(), "SAMSUNG", "4111-1111-1111-1111",
                        orderResult.totalAmount(), couponId)
        );

        // act
        PaymentCallback callback = new PaymentCallback(
                orderResult.orderId(),
                "ext-123",
                "SUCCESS",
                USER_ID,
                orderResult.totalAmount(),
                couponId,
                null
        );
        paymentService.handlePgCallback(callback);

        // assert
        assertThat(couponRepository.findById(couponId).get().isUsed()).isTrue();
        assertThat(orderRepository.findByOrderId(orderResult.orderId()).get().isPaid()).isTrue();

        verify(couponEventHandler, timeout(1000)).handle(any());
    }
}
