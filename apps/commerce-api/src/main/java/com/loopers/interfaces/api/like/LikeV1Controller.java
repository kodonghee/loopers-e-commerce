package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeUseCase;
import com.loopers.application.like.LikeInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/like/products")
public class LikeV1Controller implements LikeV1ApiSpec {

    private final LikeUseCase likeUseCase;

    @PostMapping("/{productId}")
    @Override
    public ApiResponse<Object> likeProduct(
            @RequestHeader("X-USER-ID") String userId,
            @PathVariable("productId") Long productId
    ) {
        likeUseCase.likeProduct(userId, productId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{productId}")
    @Override
    public ApiResponse<Object> cancelLikeProduct(
            @RequestHeader("X-USER-ID") String userId,
            @PathVariable("productId") Long productId
    ) {
        likeUseCase.cancelLikeProduct(userId, productId);
        return ApiResponse.success();
    }

    @GetMapping
    @Override
    public ApiResponse<List<LikeV1Dto.LikeProductResponse>> getLikedProducts(
            @RequestHeader("X-USER-ID") String userId
    ) {
        List<LikeInfo> likedProducts = likeUseCase.getLikedProducts(userId);
        List<LikeV1Dto.LikeProductResponse> response = likedProducts.stream()
                .map(LikeV1Dto.LikeProductResponse::from)
                .toList();
        return ApiResponse.success(response);
    }
}
