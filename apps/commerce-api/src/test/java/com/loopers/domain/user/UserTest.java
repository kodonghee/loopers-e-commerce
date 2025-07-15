package com.loopers.domain.user;

import com.loopers.domain.example.ExampleModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {
    @DisplayName("회원 가입 단위 테스트")
    @Nested
    class SignUp {
        @DisplayName("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @Test
        void failToCreateUser_whenIDNotSuitable() {
            // arrange
            String id = "고동희고동희";
            String gender = "F";
            String birthDate = "1995-06-11";
            String email = "qmdlfakrhf@naver.com";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new User(id, gender, birthDate, email);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("제목이 빈칸으로만 이루어져 있으면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenTitleIsBlank() {
            // arrange
            String name = "   ";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new ExampleModel(name, "설명");
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("설명이 비어있으면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenDescriptionIsEmpty() {
            // arrange
            String description = "";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new ExampleModel("제목", description);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
