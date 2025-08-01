// application/user/UserUseCaseTest.java
// 수정된 UserUseCase 단위 테스트 클래스입니다.
package com.loopers.application.user;

import com.loopers.domain.user.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserUseCase에 대한 단위 테스트 클래스입니다.
 * Mockito를 사용하여 UserRepository와 ApplicationEventPublisher의 의존성을 모의 처리합니다.
 * Mocking과 ArgumentCaptor를 활용하여 핵심 비즈니스 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserUseCase 단위 테스트")
class UserUseCaseTest {

    @InjectMocks
    private UserUseCase userUseCase;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    // UserId 유효성 검증(`영문 및 숫자 10자 이내`)에 맞는 값으로 수정
    private final UserId TEST_USER_ID = new UserId("testuser123");
    // Gender enum에 정의된 M으로 수정
    private final Gender TEST_GENDER = Gender.M;
    private final String TEST_BIRTH_DATE = "1990-01-01";
    private final String TEST_EMAIL = "test@example.com";

    @Nested
    @DisplayName("getUserInfo 메서드 테스트")
    class GetUserInfoTest {
        @Test
        @DisplayName("존재하는 사용자 ID로 조회 시, UserInfo 객체를 반환해야 한다.")
        void getUserInfo_shouldReturnUserInfo_whenUserExists() {
            // arrange
            User mockUser = new User(TEST_USER_ID.userId(), TEST_GENDER, TEST_BIRTH_DATE, TEST_EMAIL);
            when(userRepository.find(TEST_USER_ID)).thenReturn(Optional.of(mockUser));

            // act
            UserInfo result = userUseCase.getUserInfo(TEST_USER_ID);

            // assert
            assertThat(result.userId()).isEqualTo(TEST_USER_ID.userId());
            assertThat(result.gender()).isEqualTo(TEST_GENDER.name());
            assertThat(result.birthDate()).isEqualTo(TEST_BIRTH_DATE);
            assertThat(result.email()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회 시, CoreException을 던져야 한다.")
        void getUserInfo_shouldThrowException_whenUserDoesNotExist() {
            // arrange
            when(userRepository.find(TEST_USER_ID)).thenReturn(Optional.empty());

            // act & assert
            CoreException thrown = assertThrows(CoreException.class,
                    () -> userUseCase.getUserInfo(TEST_USER_ID));
            assertThat(thrown.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(thrown.getMessage()).isEqualTo("해당 ID의 회원이 없습니다.");
        }
    }

    @Nested
    @DisplayName("signUp 메서드 테스트")
    class SignUpTest {
        @Test
        @DisplayName("새로운 사용자 ID로 가입 시, User 객체를 저장하고 이벤트를 발행해야 한다.")
        void signUp_shouldSaveUserAndPublishEvent_whenUserIsNew() {
            // arrange
            // UserCommand.Create는 String으로 받으므로 .name()을 사용
            UserCommand.Create command = new UserCommand.Create(TEST_USER_ID.userId(), TEST_GENDER.name(), TEST_BIRTH_DATE, TEST_EMAIL);
            User savedUser = new User(TEST_USER_ID.userId(), TEST_GENDER, TEST_BIRTH_DATE, TEST_EMAIL);

            when(userRepository.existsByUserId(any(UserId.class))).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            ArgumentCaptor<UserSignedUpEvent> eventCaptor = ArgumentCaptor.forClass(UserSignedUpEvent.class);

            // act
            UserInfo result = userUseCase.signUp(command);

            // assert
            verify(userRepository).existsByUserId(new UserId(command.userId()));
            verify(userRepository).save(any(User.class));
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            UserSignedUpEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.userId()).isEqualTo(TEST_USER_ID);

            assertThat(result.userId()).isEqualTo(TEST_USER_ID.userId());
            assertThat(result.gender()).isEqualTo(TEST_GENDER.name());
        }

        @Test
        @DisplayName("이미 가입된 사용자 ID로 가입 시, CoreException을 던져야 한다.")
        void signUp_shouldThrowException_whenUserExists() {
            // arrange
            UserCommand.Create command = new UserCommand.Create(TEST_USER_ID.getUserId(), TEST_GENDER.name(), TEST_BIRTH_DATE, TEST_EMAIL);
            when(userRepository.existsByUserId(any(UserId.class))).thenReturn(true);

            // act & assert
            CoreException thrown = assertThrows(CoreException.class,
                    () -> userUseCase.signUp(command));

            assertThat(thrown.getErrorType()).isEqualTo(ErrorType.CONFLICT);
            assertThat(thrown.getMessage()).isEqualTo("이미 가입된 ID 입니다.");

            // 에러 발생 시, save와 publish가 호출되지 않았음을 검증
            verify(userRepository, never()).save(any(User.class));
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("유효하지 않은 userId 형식으로 가입 시, CoreException을 던져야 한다.")
        void signUp_shouldThrowException_whenUserIdIsInvalid() {
            // arrange
            UserCommand.Create command = new UserCommand.Create("invalid_id!", TEST_GENDER.name(), TEST_BIRTH_DATE, TEST_EMAIL);
            when(userRepository.existsByUserId(any(UserId.class))).thenReturn(false);

            // act & assert
            CoreException thrown = assertThrows(CoreException.class,
                    () -> userUseCase.signUp(command));

            assertThat(thrown.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(thrown.getMessage()).isEqualTo("ID는 영문 및 숫자 10자 이내여야 합니다.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 email 형식으로 가입 시, CoreException을 던져야 한다.")
        void signUp_shouldThrowException_whenEmailIsInvalid() {
            // arrange
            UserCommand.Create command = new UserCommand.Create(TEST_USER_ID.userId(), TEST_GENDER.name(), TEST_BIRTH_DATE, "invalid-email");
            when(userRepository.existsByUserId(any(UserId.class))).thenReturn(false);

            // act & assert
            CoreException thrown = assertThrows(CoreException.class,
                    () -> userUseCase.signUp(command));

            assertThat(thrown.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(thrown.getMessage()).isEqualTo("이메일은 xx@yy.zz 형식에 맞아야 합니다.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 birthDate 형식으로 가입 시, CoreException을 던져야 한다.")
        void signUp_shouldThrowException_whenBirthDateIsInvalid() {
            // arrange
            UserCommand.Create command = new UserCommand.Create(TEST_USER_ID.userId(), TEST_GENDER.name(), "1990/01/01", TEST_EMAIL);
            when(userRepository.existsByUserId(any(UserId.class))).thenReturn(false);

            // act & assert
            CoreException thrown = assertThrows(CoreException.class,
                    () -> userUseCase.signUp(command));

            assertThat(thrown.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(thrown.getMessage()).isEqualTo("생년월일은 yyyy-MM-dd 형식에 맞아야 합니다.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 gender 값으로 가입 시, CoreException을 던져야 한다.")
        void signUp_shouldThrowException_whenGenderIsInvalid() {
            // arrange
            // Gender.from() 메서드는 String을 받으므로 유효하지 않은 문자열 "C"를 전달하여 테스트
            UserCommand.Create command = new UserCommand.Create(TEST_USER_ID.userId(), "C", TEST_BIRTH_DATE, TEST_EMAIL);
            when(userRepository.existsByUserId(any(UserId.class))).thenReturn(false);

            // act & assert
            CoreException thrown = assertThrows(CoreException.class,
                    () -> userUseCase.signUp(command));

            assertThat(thrown.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(thrown.getMessage()).isEqualTo("유효하지 않은 성별 값입니다. (M 또는 F만 허용)");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("gender 값이 null일 때, CoreException을 던져야 한다.")
        void signUp_shouldThrowException_whenGenderIsNull() {
            // arrange
            // Gender.from() 메서드가 null을 받을 때 예외를 던지는지 확인
            UserCommand.Create command = new UserCommand.Create(TEST_USER_ID.userId(), null, TEST_BIRTH_DATE, TEST_EMAIL);

            // act & assert
            CoreException thrown = assertThrows(CoreException.class,
                    () -> userUseCase.signUp(command));

            assertThat(thrown.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(thrown.getMessage()).isEqualTo("성별은 필수 항목입니다.");

            verify(userRepository, never()).save(any());
        }
    }
}
