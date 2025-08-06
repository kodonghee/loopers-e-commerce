package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.LikeResult;
import com.loopers.domain.user.UserId;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/like/products")
public class LikeV1Controller implements LikeV1ApiSpec {

    private final LikeFacade likeFacade;

    @PostMapping("/{productId}")
    @Override
    public ApiResponse<Object> likeProduct(
            @RequestHeader("X-USER-ID") UserId userId,
            @PathVariable("productId") Long productId
    ) {
        likeFacade.likeProduct(userId, productId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{productId}")
    @Override
    public ApiResponse<Object> cancelLikeProduct(
            @RequestHeader("X-USER-ID") UserId userId,
            @PathVariable("productId") Long productId
    ) {
        likeFacade.cancelLikeProduct(userId, productId);
        return ApiResponse.success();
    }

    @GetMapping
    @Override
    public ApiResponse<List<LikeV1Dto.LikeProductResponse>> getLikedProducts(
            @RequestHeader("X-USER-ID") UserId userId
    ) {
        List<LikeResult> likedProducts = likeFacade.getLikedProducts(userId);
        List<LikeV1Dto.LikeProductResponse> response = likedProducts.stream()
                .map(LikeV1Dto.LikeProductResponse::from)
                .toList();
        return ApiResponse.success(response);
    }
}
