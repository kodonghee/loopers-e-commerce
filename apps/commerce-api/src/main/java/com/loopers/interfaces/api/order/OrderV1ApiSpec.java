package com.loopers.interfaces.api.order;

import com.loopers.domain.user.UserId;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Order V1 API", description = "주문 관련 API 입니다.")
public interface OrderV1ApiSpec {

    @Operation(summary = "주문 요청")
    @PostMapping("/orders")
    ApiResponse<String> placeOrder(
            @RequestHeader("X-USER-ID") UserId userId,
            @RequestBody OrderV1Dto.PlaceOrderRequest request
    );

    @Operation(summary = "유저의 주문 목록 조회")
    @GetMapping("/orders")
    ApiResponse<List<OrderV1Dto.OrderResponse>> getOrders(
            @RequestHeader("X-USER-ID") UserId userId
    );

    @Operation(summary = "단일 주문 상세 조회")
    @GetMapping("orders/{orderId}")
    ApiResponse<OrderV1Dto.OrderResponse> getOrderDetail(
            @PathVariable("orderId") String orderId
    );
}
