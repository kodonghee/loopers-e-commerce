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

@ExtendWith(MockitoExtension.class)
@DisplayName("UserUseCase 단위 테스트")
class UserFacadeTest {

    @InjectMocks
    private UserFacade userFacade;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final UserId TEST_USER_ID = new UserId("testuser12");
    private final Gender TEST_GENDER = Gender.M;
    private final String TEST_BIRTH_DATE = "1990-01-01";
    private final String TEST_EMAIL = "test@example.com";

    @Nested
    @DisplayName("getUserInfo 메서드 테스트")
    class GetUserResultTest {
        @Test
        @DisplayName("존재하는 사용자 ID로 조회 시, UserInfo 객체를 반환해야 한다.")
        void getUserInfo_shouldReturnUserInfo_whenUserExists() {
            // arrange
            User mockUser = new User(TEST_USER_ID.getUserId(), TEST_GENDER, TEST_BIRTH_DATE, TEST_EMAIL);
            when(userRepository.find(TEST_USER_ID)).thenReturn(Optional.of(mockUser));

            // act
            UserResult result = userFacade.getUserInfo(TEST_USER_ID);

            // assert
            assertThat(result.userId()).isEqualTo(TEST_USER_ID.getUserId());
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
                    () -> userFacade.getUserInfo(TEST_USER_ID));
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
            UserCriteria.Create command = new UserCriteria.Create(TEST_USER_ID.getUserId(), TEST_GENDER, TEST_BIRTH_DATE, TEST_EMAIL);
            User savedUser = new User(TEST_USER_ID.getUserId(), TEST_GENDER, TEST_BIRTH_DATE, TEST_EMAIL);

            when(userRepository.existsByUserId(any(UserId.class))).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            ArgumentCaptor<UserSignedUpEvent> eventCaptor = ArgumentCaptor.forClass(UserSignedUpEvent.class);

            // act
            UserResult result = userFacade.signUp(command);

            // assert
            verify(userRepository).existsByUserId(new UserId(command.userId()));
            verify(userRepository).save(any(User.class));
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            UserSignedUpEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.userId()).isEqualTo(TEST_USER_ID);

            assertThat(result.userId()).isEqualTo(TEST_USER_ID.getUserId());
            assertThat(result.gender()).isEqualTo(TEST_GENDER.name());
        }

        @Test
        @DisplayName("이미 가입된 사용자 ID로 가입 시, CoreException을 던져야 한다.")
        void signUp_shouldThrowException_whenUserExists() {
            // arrange
            UserCriteria.Create command = new UserCriteria.Create(TEST_USER_ID.getUserId(), TEST_GENDER, TEST_BIRTH_DATE, TEST_EMAIL);
            when(userRepository.existsByUserId(any(UserId.class))).thenReturn(true);

            // act & assert
            CoreException thrown = assertThrows(CoreException.class,
                    () -> userFacade.signUp(command));

            assertThat(thrown.getErrorType()).isEqualTo(ErrorType.CONFLICT);
            assertThat(thrown.getMessage()).isEqualTo("이미 가입된 ID 입니다.");

            verify(userRepository, never()).save(any(User.class));
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("유효하지 않은 userId 형식으로 가입 시, CoreException을 던져야 한다.")
        void signUp_shouldThrowException_whenUserIdIsInvalid() {
            // arrange
            UserCriteria.Create command = new UserCriteria.Create("invalid_id!", TEST_GENDER, TEST_BIRTH_DATE, TEST_EMAIL);

            // act & assert
            CoreException thrown = assertThrows(CoreException.class,
                    () -> userFacade.signUp(command));

            assertThat(thrown.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(thrown.getMessage()).isEqualTo("ID는 영문 및 숫자 10자 이내여야 합니다.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 email 형식으로 가입 시, CoreException을 던져야 한다.")
        void signUp_shouldThrowException_whenEmailIsInvalid() {
            // arrange
            UserCriteria.Create command = new UserCriteria.Create(TEST_USER_ID.getUserId(), TEST_GENDER, TEST_BIRTH_DATE, "invalid-email");

            // act & assert
            CoreException thrown = assertThrows(CoreException.class,
                    () -> userFacade.signUp(command));

            assertThat(thrown.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(thrown.getMessage()).isEqualTo("이메일은 xx@yy.zz 형식에 맞아야 합니다.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 birthDate 형식으로 가입 시, CoreException을 던져야 한다.")
        void signUp_shouldThrowException_whenBirthDateIsInvalid() {
            // arrange
            UserCriteria.Create command = new UserCriteria.Create(TEST_USER_ID.getUserId(), TEST_GENDER, "1990/01/01", TEST_EMAIL);

            // act & assert
            CoreException thrown = assertThrows(CoreException.class,
                    () -> userFacade.signUp(command));

            assertThat(thrown.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(thrown.getMessage()).isEqualTo("생년월일은 yyyy-MM-dd 형식에 맞아야 합니다.");

            verify(userRepository, never()).save(any());
        }

    }
}
