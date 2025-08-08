package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrandTest {
    @DisplayName("브랜드 이름이 빈 값이면 브랜드가 생성되지 않는다.")
    @Test
    void failToCreateBrand_whenNameInvalid() {
        // arrange
        String invalidName = "";

        // act
        IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> {
            new Brand(invalidName);
        });

        // assert
        assertThat(result.getMessage()).contains("브랜드명");
    }

    @DisplayName("정상적인 브랜드 이름이면 브랜드 생성에 성공한다.")
    @Test
    void createBrand_whenValidName() {
        // arrange
        String name = "나이키";

        // act
        Brand brand = new Brand(name);

        // assert
        assertThat(brand.getName()).isEqualTo("나이키");
    }
}
