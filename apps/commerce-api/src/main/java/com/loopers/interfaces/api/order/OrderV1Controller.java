package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderService;
import com.loopers.application.order.OrderResult;
import com.loopers.application.order.port.OrderEventSender;
import com.loopers.domain.order.PaymentMethod;
import com.loopers.domain.user.UserId;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderService orderService;
    private final OrderEventSender orderEventSender;

    @PostMapping
    @Override
    public ApiResponse<Long> placeOrder(
            @RequestHeader("X-USER-ID") UserId userId,
            @RequestBody OrderV1Dto.PlaceOrderRequest request
    ) {
        OrderCriteria criteria = new OrderCriteria(
                userId.getUserId(),
                request.getItems().stream()
                        .map(i -> new OrderCriteria.OrderLine(i.productId(), i.quantity(), i.price()))
                        .toList(),
                request.getCouponId(),
                PaymentMethod.valueOf(request.getPaymentMethod())
        );
        OrderResult orderResult = orderService.placeOrder(criteria);
        orderEventSender.send(orderResult.orderId());
        return ApiResponse.success(OrderV1Dto.OrderResponse.from(orderResult).orderId());
    }

    @GetMapping
    @Override
    public ApiResponse<List<OrderV1Dto.OrderResponse>> getOrders(
            @RequestHeader("X-USER-ID") UserId userId
    ) {
        List<OrderResult> orders = orderService.getOrderList(userId);
        return ApiResponse.success(orders.stream()
                .map(OrderV1Dto.OrderResponse::from).toList());
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> getOrderDetail(
            @PathVariable("orderId") Long orderId
    ) {
        OrderResult info = orderService.getOrderDetail(orderId);
        return ApiResponse.success(OrderV1Dto.OrderResponse.from(info));
    }
}
