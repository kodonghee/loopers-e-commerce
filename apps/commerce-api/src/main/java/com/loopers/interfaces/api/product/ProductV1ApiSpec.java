package com.loopers.interfaces.api.product;

import com.loopers.domain.product.ProductSearchCondition;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Product V1 API", description = "상품 관련 API 입니다.")
public interface ProductV1ApiSpec {

    @Operation(
        summary = "상품 목록 조회",
        description = "브랜드 ID, 정렬 기준, 페이지 번호, 페이지당 상품 수에 따라 상품 목록을 조회합니다."
    )
    @GetMapping("")
    ApiResponse<List<ProductV1Dto.ProductListResponse>> getProductList(
            @Parameter(description = "브랜드 ID (선택)", example = "1")
            @RequestParam(name = "brandId", required = false)
            Long brandId,

            @Parameter(description = "정렬 기준 (latest, price_asc, likes_desc)", example = "latest")
            @RequestParam(name = "sort", defaultValue = "latest")
            ProductSearchCondition.ProductSortType sortType,

            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @Parameter(description = "페이지당 상품 수", example = "20")
            @RequestParam(name = "size", defaultValue = "20")
            int size
    );

    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{productId}")
    ApiResponse<ProductV1Dto.ProductDetailResponse> getProductDetail(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable("productId")
            Long productId
    );
}
