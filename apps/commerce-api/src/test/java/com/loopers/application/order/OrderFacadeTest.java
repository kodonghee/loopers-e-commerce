package com.loopers.application.order;

import com.loopers.application.order.OrderCriteria.OrderLine;
import com.loopers.application.order.port.OrderEventSender;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @InjectMocks
    private OrderFacade orderFacade;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderEventSender orderEventSender;

    private static final String USER_ID = "user1";
    private static final Long ORDER_ID = 1L;

    @DisplayName("placeOrder 메서드 테스트")
    @Nested
    class PlaceOrderTest {
        @DisplayName("정상적인 주문 생성 요청 시, 주문을 성공적으로 처리하고 Order ID를 반환한다.")
        @Test
        void placeOrder_shouldSucceed_andReturnOrderId() {
            // arrange
            OrderCriteria command = new OrderCriteria(
                    USER_ID,
                    List.of(new OrderCriteria.OrderLine(1L, 2, new BigDecimal("1000")),
                            new OrderCriteria.OrderLine(2L, 1, new BigDecimal("500")))
            );

            Order mockOrder = mock(Order.class);
            when(mockOrder.getId()).thenReturn(ORDER_ID);
            when(orderService.createOrder(eq(USER_ID), any())).thenReturn(mockOrder);

            // act
            OrderResult orderResult = orderFacade.placeOrder(command);

            // assert
            assertThat(orderResult.orderId()).isEqualTo(ORDER_ID);

            ArgumentCaptor<List<com.loopers.domain.order.OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
            verify(orderService).createOrder(eq(USER_ID), itemsCaptor.capture());

            List<com.loopers.domain.order.OrderItem> capturedItems = itemsCaptor.getValue();
            assertThat(capturedItems).hasSize(2);
            assertThat(capturedItems.get(0).getProductId()).isEqualTo(1L);
            assertThat(capturedItems.get(0).getQuantity()).isEqualTo(2);
            assertThat(capturedItems.get(0).getPrice()).isEqualByComparingTo(new BigDecimal("1000"));
            assertThat(capturedItems.get(1).getProductId()).isEqualTo(2L);
            assertThat(capturedItems.get(1).getQuantity()).isEqualTo(1);
            assertThat(capturedItems.get(1).getPrice()).isEqualByComparingTo(new BigDecimal("500"));

            verify(orderRepository).save(mockOrder);

            verify(orderEventSender).send(ORDER_ID);
        }
    }

    @DisplayName("getOrderList 메서드 테스트")
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
    }
}
