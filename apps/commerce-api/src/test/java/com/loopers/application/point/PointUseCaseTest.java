package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserSignedUpEvent;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointUseCase 단위 테스트")
class PointUseCaseTest {

    @InjectMocks
    private PointUseCase pointUseCase;

    @Mock
    private PointRepository pointRepository;

    private static final String USER_ID_STRING = "testUser1";
    private static final UserId USER_ID = new UserId(USER_ID_STRING);
    private static final BigDecimal INITIAL_AMOUNT = new BigDecimal("1000");
    private static final BigDecimal CHARGE_AMOUNT = new BigDecimal("500");

    @Nested
    @DisplayName("getPoints 메서드 테스트")
    class GetPointsTest {
        @Test
        @DisplayName("해당 유저의 포인트가 존재할 때, 포인트 값을 반환해야 한다.")
        void getPoints_shouldReturnPointValue_whenUserExists() {
            // arrange
            Point mockPoint = new Point(USER_ID_STRING, INITIAL_AMOUNT);
            when(pointRepository.find(USER_ID)).thenReturn(Optional.of(mockPoint));

            // act
            BigDecimal result = pointUseCase.getPoints(USER_ID);

            // assert
            assertThat(result).isEqualTo(INITIAL_AMOUNT);
            verify(pointRepository).find(USER_ID);
        }

        @Test
        @DisplayName("해당 유저의 포인트가 존재하지 않을 때, null을 반환해야 한다.")
        void getPoints_shouldReturnNull_whenUserDoesNotExist() {
            // arrange
            when(pointRepository.find(USER_ID)).thenReturn(Optional.empty());

            // act
            BigDecimal result = pointUseCase.getPoints(USER_ID);

            // assert
            assertThat(result).isNull();
            verify(pointRepository).find(USER_ID);
        }
    }

    @Nested
    @DisplayName("chargePoints 메서드 테스트")
    class ChargePointsTest {
        @Test
        @DisplayName("포인트 충전 시, 유저의 포인트가 올바르게 증가하고 새로운 잔액을 반환해야 한다.")
        void chargePoints_shouldIncreasePointsAndReturnNewBalance() {
            // arrange
            Point existingPoint = new Point(USER_ID_STRING, INITIAL_AMOUNT);
            when(pointRepository.find(USER_ID)).thenReturn(Optional.of(existingPoint));

            // act
            BigDecimal result = pointUseCase.chargePoints(USER_ID, CHARGE_AMOUNT);

            // assert
            BigDecimal expectedBalance = INITIAL_AMOUNT.add(CHARGE_AMOUNT);
            assertThat(result).isEqualTo(expectedBalance);
            assertThat(existingPoint.getPointValue()).isEqualTo(expectedBalance);
            verify(pointRepository).find(USER_ID);
        }

        @Test
        @DisplayName("포인트 충전 시, 유저가 존재하지 않으면 CoreException을 던져야 한다.")
        void chargePoints_shouldThrowException_whenUserDoesNotExist() {
            // arrange
            when(pointRepository.find(USER_ID)).thenReturn(Optional.empty());

            // act & assert
            CoreException thrownException = assertThrows(CoreException.class,
                    () -> pointUseCase.chargePoints(USER_ID, CHARGE_AMOUNT));
            assertThat(thrownException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(thrownException.getMessage()).isEqualTo("해당 ID의 회원이 없습니다.");
            verify(pointRepository).find(USER_ID);
        }
    }

    @Nested
    @DisplayName("handleUserSignedUpEvent 메서드 테스트")
    class HandleUserSignedUpEventTest {
        @Test
        @DisplayName("회원 가입 이벤트 발생 시, 0 포인트를 가진 Point 객체를 저장해야 한다.")
        void handleUserSignedUpEvent_shouldSaveNewPointWithZero() {
            // arrange
            UserSignedUpEvent event = new UserSignedUpEvent(USER_ID);
            ArgumentCaptor<Point> pointCaptor = ArgumentCaptor.forClass(Point.class);

            // act
            pointUseCase.handleUserSignedUpEvent(event);

            // assert
            verify(pointRepository).save(pointCaptor.capture());
            Point savedPoint = pointCaptor.getValue();
            assertThat(savedPoint.getUserId()).isEqualTo(USER_ID_STRING);
            assertThat(savedPoint.getPointValue()).isEqualTo(BigDecimal.ZERO);
        }
    }
}
