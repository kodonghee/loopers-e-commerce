package com.loopers.interfaces.api.user;

import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.point.PointV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserV1ApiE2ETest {

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public UserV1ApiE2ETest(
            TestRestTemplate testRestTemplate,
            UserJpaRepository userJpaRepository,
            DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
    }

    private static final String USER_ID = "gdh5866";
    private static final String GENDER = "F";
    private static final String BIRTH_DATE = "1995-06-11";
    private static final String EMAIL = "donghee@test.com";
    private static final String SIGN_UP_ENDPOINT = "/api/v1/users";
    private static final String INFO_CHECK_ENDPOINT = "/api/v1/users/me";

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private UserV1Dto.UserRequest defaultUserRequest() {
        return new UserV1Dto.UserRequest(USER_ID, GENDER, BIRTH_DATE, EMAIL);
    }

    @DisplayName("POST /api/v1/users")
    @Nested
    class SignUp {
        @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenSignUpIsSuccessful() {
            // arrange
            HttpEntity<UserV1Dto.UserRequest> httpEntity = new HttpEntity<>(defaultUserRequest());

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(SIGN_UP_ENDPOINT, HttpMethod.POST, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().data().userId()).isEqualTo(USER_ID),
                    () -> assertThat(response.getBody().data().gender()).isEqualTo(GENDER),
                    () -> assertThat(response.getBody().data().birthDate()).isEqualTo(BIRTH_DATE),
                    () -> assertThat(response.getBody().data().email()).isEqualTo(EMAIL)
            );
        }

        @DisplayName("회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenGenderIsMissing() {
            // arrange
            var request = new UserV1Dto.UserRequest(USER_ID, "", BIRTH_DATE, EMAIL);
            HttpEntity<UserV1Dto.UserRequest> httpEntity = new HttpEntity<>(request);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(SIGN_UP_ENDPOINT, HttpMethod.POST, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );

        }

    }

    @DisplayName("GET /api/v1/users/me")
    @Nested
    class InfoCheck {
        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenInfoCheckIsSuccessful() {
            // arrange
            testRestTemplate.postForEntity(SIGN_UP_ENDPOINT, defaultUserRequest(), Void.class);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", USER_ID);
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(INFO_CHECK_ENDPOINT, HttpMethod.GET, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().data().userId()).isEqualTo(USER_ID),
                    () -> assertThat(response.getBody().data().gender()).isEqualTo(GENDER),
                    () -> assertThat(response.getBody().data().birthDate()).isEqualTo(BIRTH_DATE),
                    () -> assertThat(response.getBody().data().email()).isEqualTo(EMAIL)
            );
        }

        @DisplayName("존재하지 않는 ID 로 조회할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returns404NotFound_whenIDDoesNotExist() {
            // arrange
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", USER_ID);
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(INFO_CHECK_ENDPOINT, HttpMethod.GET, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }

    }
}

