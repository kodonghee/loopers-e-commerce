package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LikeTest {

    @Nested
    @DisplayName("Like 엔티티 생성")
    class CreateLike {

        @Test
        @DisplayName("유효한 값으로 Like 엔티티 생성 시 성공한다")
        void createLike_withValidValues_shouldSucceed() {
            // Arrange
            String userId = "user123";
            Long productId = 1L;

            // Act & Assert
            assertDoesNotThrow(() -> new Like(userId, productId));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("userId가 null이거나 비어있으면 예외를 던진다")
        void createLike_withInvalidUserId_shouldThrowException(String invalidUserId) {
            // Arrange
            Long productId = 1L;

            // Act & Assert
            CoreException exception = assertThrows(CoreException.class, () -> new Like(invalidUserId, productId));
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("사용자 ID는 필수 입력 값 입니다.");
        }

        @Test
        @DisplayName("productId가 null이면 예외를 던진다")
        void createLike_withNullProductId_shouldThrowException() {
            // Arrange
            String userId = "user123";
            Long invalidProductId = null;

            // Act & Assert
            CoreException exception = assertThrows(CoreException.class, () -> new Like(userId, invalidProductId));
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("상품 ID는 필수 입력 값 입니다.");
        }
    }

    // Like 엔티티의 동등성 관련 테스트
    @Nested
    @DisplayName("Like 엔티티 동등성")
    class LikeEquality {

        @Test
        @DisplayName("userId와 productId가 같으면 같은 객체로 간주한다")
        void twoLikes_withSameUserIdAndProductId_shouldBeEqual() {
            // Arrange
            String userId = "user123";
            Long productId = 1L;
            Like like1 = new Like(userId, productId);
            Like like2 = new Like(userId, productId);

            // Act & Assert
            assertThat(like1).isEqualTo(like2);
            assertThat(like1.hashCode()).isEqualTo(like2.hashCode());
        }

        @Test
        @DisplayName("userId가 다르면 다른 객체로 간주한다")
        void twoLikes_withDifferentUserId_shouldNotBeEqual() {
            // Arrange
            String userId1 = "user123";
            String userId2 = "user456";
            Long productId = 1L;
            Like like1 = new Like(userId1, productId);
            Like like2 = new Like(userId2, productId);

            // Act & Assert
            assertThat(like1).isNotEqualTo(like2);
        }

        @Test
        @DisplayName("productId가 다르면 다른 객체로 간주한다")
        void twoLikes_withDifferentProductId_shouldNotBeEqual() {
            // Arrange
            String userId = "user123";
            Long productId1 = 1L;
            Long productId2 = 2L;
            Like like1 = new Like(userId, productId1);
            Like like2 = new Like(userId, productId2);

            // Act & Assert
            assertThat(like1).isNotEqualTo(like2);
        }
    }
}
