package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCommand;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface OrderV1ApiSpec {

    @Operation(summary = "주문 생성")
    @PostMapping
    ApiResponse<Long> placeOrder(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody OrderCommand.OrderItemRequestList request
    );

    @Operation(summary = "내 주문 목록 조회")
    @GetMapping
    ApiResponse<List<OrderV1Dto.OrderResponse>> getOrders(
            @RequestHeader("X-USER-ID") String userId
    );

    @Operation(summary = "주문 상세 조회")
    @GetMapping("/{orderId}")
    ApiResponse<OrderV1Dto.OrderResponse> getOrderDetail(
            @PathVariable("orderId") Long orderId
    );
}
