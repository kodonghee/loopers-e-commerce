package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.BrandReader;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/brands")
public class BrandV1Controller implements BrandV1ApiSpec {

    private final BrandReader brandReader;

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandV1Dto.BrandResponse> getBrand(
            @PathVariable("brandId") Long brandId
    ) {
        String name = brandReader.getBrandName(brandId);
        return ApiResponse.success(BrandV1Dto.BrandResponse.from(brandId, name));
    }
}
