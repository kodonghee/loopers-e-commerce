package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderUseCase;
import com.loopers.application.order.OrderInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderUseCase orderUseCase;

    @Override
    public ApiResponse<Long> placeOrder(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody OrderCommand.OrderItemRequestList request
    ) {
        OrderCommand command = new OrderCommand(userId, request.items());
        Long id = orderUseCase.placeOrder(command);
        return ApiResponse.success(id);
    }

    @Override
    public ApiResponse<List<OrderV1Dto.OrderResponse>> getOrders(String userId) {
        List<OrderInfo> orders = orderUseCase.getOrderList(userId);
        return ApiResponse.success(orders.stream()
                .map(OrderV1Dto.OrderResponse::from).toList());
    }

    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> getOrderDetail(Long orderId) {
        OrderInfo info = orderUseCase.getOrderDetail(orderId);
        return ApiResponse.success(OrderV1Dto.OrderResponse.from(info));
    }
}
