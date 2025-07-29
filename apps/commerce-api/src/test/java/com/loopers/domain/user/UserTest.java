package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @DisplayName("회원 가입 단위 테스트")
    @Nested
    class SignUp {
        @DisplayName("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않으면, User 객체 생성에 실패 한다.")
        @Test
        void failToCreateUser_whenIDNotSuitable() {
            // arrange
            String userId = "고동희고동희";
            String birthDate = "1995-06-11";
            String email = "qmdlfakrhf@naver.com";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new User(userId, Gender.F, birthDate, email);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("ID"); // ID 때문에 에러가 발생 했는지 정확하게 확인
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "a",                // 유효한 최소 길이 (1자)
                "abcde",            // 유효한 중간 길이
                "abcdefghij",       // 유효한 최대 길이 (10자)
                "user12345",        // 영문/숫자 조합
                "USERABCDEF",       // 대문자 조합
                "1234567890"        // 숫자 조합
        })
        @DisplayName("유효한 ID로 User 객체 생성 성공 (경계값 포함)")
        void createUserWithValidId(String validId) {
            String birthDate = "1995-06-11";
            String email = "test@example.com";

            assertDoesNotThrow(() -> new User(validId, Gender.F, birthDate, email));
        }

        @DisplayName("이메일이 xx@yy.zz 형식에 맞지 않으면, User 객체 생성에 실패 한다.")
        @Test
        void failToCreateUser_whenEmailNotSuitable() {
            // arrange
            String userId = "gdh5866";
            String birthDate = "1995-06-11";
            String email = "trashtrash";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new User(userId, Gender.F, birthDate, email);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("이메일"); // 이메일 때문에 에러가 발생 했는지 정확하게 확인
        }

        @DisplayName("생년월일이 yyyy-MM-dd 형식에 맞지 않으면, User 객체 생성에 실패 한다.")
        @Test
        void failToCreateUser_whenBirthDateNotSuitable() {
            // arrange
            String userId = "gdh5866";
            String birthDate = "19950611";
            String email = "test@email.com";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new User(userId, Gender.F, birthDate, email);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("생년월일"); // 생년월일 때문에 에러가 발생 했는지 정확하게 확인
        }
    }
}
