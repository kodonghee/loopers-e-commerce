package com.loopers.domain.user;

import com.loopers.infrastructure.user.UserJpaRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockitoSpyBean
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("회원 가입 통합 테스트")
    @Nested
    class SignUp {
        @DisplayName("회원 가입시 User 저장이 수행 된다.")
        @Test
        void saveUser_whenSignUp() {
            // arrange
            String userId = "gdh5866";
            UserCommand.Create command = new UserCommand.Create(
                    userId,
                    "F",
                    "1995-06-11",
                    "donghee@test.com"
            );

            // act
            userService.signUp(command);

            // assert
            verify(userJpaRepository, times(1)).save(any(User.class));

            Optional<User> savedUser = userJpaRepository.findByUserId(userId);
            assertThat(savedUser).isPresent();
            assertThat(savedUser.get().getEmail()).isEqualTo("donghee@test.com");
        }

        @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
        @Test
        void failToSignUp_whenTryToSignUpSavedID() {
            // arrange
            String sameId = "gdh5866";

            UserCommand.Create firstCommand = new UserCommand.Create(
                    sameId,
                    "F",
                    "1995-06-11",
                    "donghee@test.com"
            );

            // 사전 저장
            userService.signUp(firstCommand);

            // ID만 같고, 나머지 요소는 다른 두 번째 요청 (ID 중복 시 실패 한다는 것을 더 정확하게 테스트하기 위함)
            UserCommand.Create secondCommand = new UserCommand.Create (
                    sameId,
                    "M",
                    "2000-01-01",
                    "another@mail.com"
            );

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                userService.signUp(secondCommand);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.CONFLICT);
            assertThat(result.getMessage()).contains("이미 가입된 ID");
        }
    }

    @DisplayName("내 정보 조회 통합 테스트")
    @Nested
    class InfoCheck {
        @DisplayName("해당 ID의 회원이 존재할 경우, 회원 정보가 반환된다.")
        @Test
        void returnUserInfo_whenUserExists() {
            // arrange
            String userId = "gdh5866";
            String gender = "F";
            String birthDate = "1995-06-11";
            String email = "donghee@test.com";

            User user = new User(userId, gender, birthDate, email);
            userRepository.save(user);

            // act
            UserInfo userInfo = userService.getUserInfo(new UserId(userId));

            // assert
            assertThat(userInfo).isNotNull();
            assertThat(userInfo.userId()).isEqualTo(userId);
            assertThat(userInfo.gender()).isEqualTo(gender);
            assertThat(userInfo.birthDate()).isEqualTo(birthDate);
            assertThat(userInfo.email()).isEqualTo(email);
        }


        @DisplayName("해당 ID의 회원이 존재하지 않을 경우, 예외가 발생한다.")
        @Test
        void returnNull_whenUserDoesNotExist() {
            // arrange
            String unSavedId = "somebody";

            // acT
            CoreException result = assertThrows(CoreException.class, () -> {
                userService.getUserInfo(new UserId(unSavedId));
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

    }

    @DisplayName("포인트 조회 통합 테스트")
    @Nested
    class PointCheck {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void returnsPointsOnHand_whenUserExists() {
            // arrange
            String userId = "gdh5866";
            String gender = "F";
            String birthDate = "1995-06-11";
            String email = "donghee@test.com";
            User user = new User(userId, gender, birthDate, email);
            userRepository.save(user);

            // act
            Long point = userService.getPoints(new UserId(userId));

            // assert
            assertThat(point).isNotNull();
            assertThat(point).isEqualTo(0L);
        }


        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnNull_whenUserDoesNotExist() {
            // arrange
            String unSavedId = "somebody";

            // act
            Long point = userService.getPoints(new UserId(unSavedId));

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
                userService.chargePoints(new UserId(unSavedId), 1000L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(result.getMessage()).contains("ID");
        }

    }
}
