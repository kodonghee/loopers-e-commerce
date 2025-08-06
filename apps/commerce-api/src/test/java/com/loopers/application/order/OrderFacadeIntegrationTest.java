package com.loopers.application.order;

import com.loopers.application.coupon.CouponCriteria;
import com.loopers.application.coupon.CouponUseCase;
import com.loopers.application.order.port.OrderEventSender;
import com.loopers.application.point.PointFacade;
import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductMapper;
import com.loopers.application.product.ProductFacade;
import com.loopers.application.user.UserFacade;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Money;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.Stock;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.aspectj.weaver.ast.Or;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.loopers.application.product.ProductMapper.fromProduct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private CouponUseCase couponUseCase;
    @Autowired
    private OrderRepository orderRepository;
    @MockitoSpyBean
    private OrderEventSender orderEventSender;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String USER_ID = "cookie95";
    @BeforeEach
    void setUp() {
        userRepository.save(new User(USER_ID, Gender.F, "1995-06-11", "test@naver.com"));
        Point POINT = pointRepository.save(new Point(USER_ID, new BigDecimal("300000")));
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
            Product product = productFacade.create(new ProductCriteria("스니커즈", 10, new BigDecimal("100000"), 1L));
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
            /*assertThat(result.totalAmount()).isEqualTo(new BigDecimal("15000"));
            assertThat(orderRepository.findById(result.orderId())).isPresent();
            assertThat(couponRepository.findById(coupon.getId()).get().isUsed()).isTrue();
            assertThat(productRepository.findById(product.getId()).get().getStock()).isEqualTo(8);
            assertThat(pointRepository.find(new UserId(USER_ID)).get().getAmount())
                    .isEqualTo(new BigDecimal("15000"));*/ // 이벤트 전송 검증 .. 해야하나?
        }

        @DisplayName("재고 부족으로 주문 실패 시, 포인트 차감이나 쿠폰 사용이 없어야 한다.")
        @Test
        void shouldRollbackAll_whenStockIsInsufficient() {

        }

        @DisplayName("포인트 부족으로 주문 실패 시, 재고 차감이나 쿠폰 사용이 없어야 한다.")
        @Test
        void shouldRollbackAll_whenPointIsInsufficient() {

        }

        @DisplayName("잘못된 쿠폰 사용 시, 전체 주문이 실패한다.")
        @Test
        void shouldRollbackAll_whenCouponInvalid() {

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
