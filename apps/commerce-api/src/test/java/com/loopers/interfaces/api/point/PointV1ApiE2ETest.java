package com.loopers.interfaces.api.point;

import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.UserV1Dto;
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
class PointV1ApiE2ETest {

    private final TestRestTemplate testRestTemplate;
    private final UserJpaRepository userJpaRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public PointV1ApiE2ETest(
            TestRestTemplate testRestTemplate,
            UserJpaRepository userJpaRepository,
            DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.userJpaRepository = userJpaRepository;
        this.databaseCleanUp = databaseCleanUp;
    }
    private static final String USER_ID = "gdh5866";
    private static final String GENDER = "F";
    private static final String BIRTH_DATE = "1995-06-11";
    private static final String EMAIL = "donghee@test.com";
    private static final String SIGN_UP_ENDPOINT = "/api/v1/users";
    private static final String POINT_CHECK_ENDPOINT = "/api/v1/points";
    private static final String POINT_CHARGE_ENDPOINT = "/api/v1/points/charge";

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private UserV1Dto.UserRequest defaultUserRequest() {
        return new UserV1Dto.UserRequest(USER_ID, GENDER, BIRTH_DATE, EMAIL);
    }
    @DisplayName("GET /api/v1/points")
    @Nested
    class PointCheck {
        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
        @Test
        void returnsPoints_whenPointCheckIsSuccessful() {
            // arrange
            testRestTemplate.postForEntity(SIGN_UP_ENDPOINT, defaultUserRequest(), Void.class);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", USER_ID);
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<Long>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<Long>> response =
                    testRestTemplate.exchange(POINT_CHECK_ENDPOINT, HttpMethod.GET, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().data()).isEqualTo(0L)
            );
        }

        @DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returns404BadRequest_whenXUSERIDDoesNotExist() {
            // arrange
            HttpEntity<Void> httpEntity = new HttpEntity<>(null);

            // act
            ParameterizedTypeReference<ApiResponse<Long>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<Long>> response =
                    testRestTemplate.exchange(POINT_CHECK_ENDPOINT, HttpMethod.GET, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }
    }

    @DisplayName("POST /api/v1/points/charge")
    @Nested
    class PointCharge {
        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void returnsTotalPoints_whenUserAddsPoints1000() {
            // arrange
            testRestTemplate.postForEntity(SIGN_UP_ENDPOINT, defaultUserRequest(), Void.class);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", USER_ID);
            PointV1Dto.PointChargeRequest request = new PointV1Dto.PointChargeRequest(1000L);
            HttpEntity<PointV1Dto.PointChargeRequest> httpEntity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<Long>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Long>> response =
                    testRestTemplate.exchange(POINT_CHARGE_ENDPOINT, HttpMethod.POST, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().data()).isEqualTo(1000L)
            );
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returns404NotFound_whenUserDoesNotExist() {
            // arrange
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", USER_ID);
            PointV1Dto.PointChargeRequest request = new PointV1Dto.PointChargeRequest(1000L);
            HttpEntity<PointV1Dto.PointChargeRequest> httpEntity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<Long>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Long>> response =
                    testRestTemplate.exchange(POINT_CHARGE_ENDPOINT, HttpMethod.POST, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }

    }
}
