package com.loopers.interfaces.api.like;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Like V1 API", description = "좋아요 관련 API 입니다.")
public interface LikeV1ApiSpec {

    @Operation(summary = "상품 좋아요 등록")
    @PostMapping("/products/{productId}")
    ApiResponse<Object> likeProduct(
            @Parameter(hidden = true) @RequestHeader("X-USER-ID") String userId,
            @PathVariable Long productId
    );

    @Operation(summary = "상품 좋아요 취소")
    @DeleteMapping("/products/{productId}")
    ApiResponse<Object> cancelLikeProduct(
            @Parameter(hidden = true) @RequestHeader("X-USER-ID") String userId,
            @PathVariable Long productId
    );

    @Operation(summary = "내가 좋아요 한 상품 목록 조회")
    @GetMapping("/products")
    ApiResponse<List<LikeV1Dto.LikeProductResponse>> getLikedProducts(
            @Parameter(hidden = true) @RequestHeader("X-USER-ID") String userId
    );
}
