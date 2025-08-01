package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class PointUseCaseIntegrationTest {
    @Autowired
    private PointUseCase pointUseCase;

    @Autowired
    private PointRepository pointRepository;

    @MockitoSpyBean
    private PointJpaRepository pointJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트 조회 통합 테스트")
    @Nested
    class PointCheck {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void returnsPointsOnHand_whenUserExists() {
            // arrange
            String userId = "gdh5866";
            String birthDate = "1995-06-11";
            String email = "donghee@test.com";
            User user = new User(userId, Gender.F, birthDate, email);
            pointRepository.save(new Point(user.getUserId(), BigDecimal.ZERO));

            // act
            BigDecimal point = pointUseCase.getPoints(new UserId(userId));

            // assert
            assertThat(point).isNotNull();
            assertThat(point.compareTo(BigDecimal.ZERO)).isEqualTo(0);
        }


        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnNull_whenUserDoesNotExist() {
            // arrange
            String unSavedId = "somebody";

            // act
            BigDecimal point = pointUseCase.getPoints(new UserId(unSavedId));

            // assert
            assertThat(point).isNull();
        }

    }

    @DisplayName("포인트 충전 통합 테스트")
    @Nested
    class PointCharge {
        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void failToCharge_whenUserDoesNotExist() {
            // arrange
            String unSavedId = "somebody";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                pointUseCase.chargePoints(new UserId(unSavedId), BigDecimal.valueOf(1000));
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(result.getMessage()).contains("ID");
        }
    }
}
