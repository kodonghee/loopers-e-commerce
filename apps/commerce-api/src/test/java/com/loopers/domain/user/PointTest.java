package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PointTest {
    @DisplayName("포인트 충전 단위 테스트")
    @Nested
    class PointCharge {
        @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
        @Test
        void failToCharge_whenChargeAmountIsZeroOrNegative() {
            // arrange
            User user = new User("gdh5866", "F", "1995-06-11", "donghee@test.com");

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                user.chargePoint(0L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("충전할 포인트는 0보다 커야 합니다.");
        }
    }
}
