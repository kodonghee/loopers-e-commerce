package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductUseCase;
import com.loopers.domain.product.ProductSearchCondition;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductUseCase productUseCase;

    @GetMapping
    @Override
    public ApiResponse<List<ProductV1Dto.ProductResponse>> getProductList(
            @RequestParam(required = false) Long brandId,
            @RequestParam(defaultValue = "LATEST") ProductSearchCondition.ProductSortType sortType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ProductSearchCondition condition = new ProductSearchCondition(
                brandId, sortType, page, size
        );
        List<ProductInfo> products = productUseCase.getProductList(condition);
        List<ProductV1Dto.ProductResponse> response = products.stream()
                .map(ProductV1Dto.ProductResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.ProductResponse> getProductDetail(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable("productId") Long productId
    ) {
        ProductInfo product = productUseCase.getProductDetail(productId);
        return ApiResponse.success(ProductV1Dto.ProductResponse.from(product));
    }

}
