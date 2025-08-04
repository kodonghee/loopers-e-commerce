package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BrandReaderImpl implements BrandReader {

    private final BrandJpaRepository brandJpaRepository;

    @Override
    public String getBrandName(Long brandId) {
        Brand brand = brandJpaRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다."));
        return brand.getName();
    }
}
