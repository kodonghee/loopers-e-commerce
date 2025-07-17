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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class USerServiceIntegrationTest {
    @Autowired
    private UserService userService;

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
            UserCommand.Create command = new UserCommand.Create(
                    "gdh5866",
                    "F",
                    "1995-06-11",
                    "donghee@test.com"
            );

            // act
            userService.signUp(command);

            // assert
            verify(userJpaRepository, times(1)).save(any(User.class));
        }

        @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
        @Test
        void failToSignUp_whenTryToSignUpExistedID() {
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

        private final String userId = "gdh5866";
        private final String gender = "F";
        private final String birthDate = "1995-06-11";
        private final String email = "donghee@test.com";

        @DisplayName("해당 ID의 회원이 존재할 경우, 회원 정보가 반환된다.")
        @Test
        void returnUserInfo_whenUserExists() {
            // arrange
            UserCommand.Create command = new UserCommand.Create(userId, gender, birthDate, email);
            userService.signUp(command);

            // act
            UserInfo userInfo = userService.getUserInfo(userId);

            // assert
            assertThat(userInfo).isNotNull();
            assertThat(userInfo.userId()).isEqualTo(userId);
            assertThat(userInfo.gender()).isEqualTo(gender);
            assertThat(userInfo.birthDate()).isEqualTo(birthDate);
            assertThat(userInfo.email()).isEqualTo(email);
        }
    }
}
