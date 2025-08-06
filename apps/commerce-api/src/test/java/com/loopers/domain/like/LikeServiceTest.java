package com.loopers.domain.like;

import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LikeDomainService 단위 테스트")
class LikeServiceTest {

    private LikeRepository likeRepository;
    private LikeService likeService;

    private static class FakeLikeRepository implements LikeRepository {
        private final Map<String, Like> likes = new HashMap<>();

        @Override
        public void save(Like like) {
            String key = like.getUserId() + "_" + like.getProductId();
            likes.put(key, like);
        }

        @Override
        public Optional<Like> findByUserIdAndProductId(UserId userId, Long productId) {
            String key = userId.getUserId() + "_" + productId;
            return Optional.ofNullable(likes.get(key));
        }

        @Override
        public void delete(Like like) {
            String key = like.getUserId() + "_" + like.getProductId();
            likes.remove(key);
        }

        @Override
        public List<Like> findAllByUserId(UserId userId) {
            return likes.values().stream()
                    .filter(like -> like.getUserId().equals(userId.getUserId()))
                    .collect(Collectors.toList());
        }

        @Override
        public long countByProductId(Long productId) {
            return likes.values().stream()
                    .filter(like -> like.getProductId().equals(productId))
                    .count();
        }
    }

    @BeforeEach
    void setUp() {
        likeRepository = new FakeLikeRepository();
        likeService = new LikeService(likeRepository);
    }

    @Nested
    @DisplayName("좋아요 등록 기능 테스트")
    class AddLike {

        @Test
        @DisplayName("좋아요를 성공적으로 등록한다.")
        void shouldAddLikeSuccessfully() {
            // Arrange
            UserId userId = new UserId("user1");
            Long productId = 1L;

            // Act
            likeService.addLike(userId, productId);

            // Assert
            assertTrue(likeRepository.findByUserIdAndProductId(userId, productId).isPresent());
        }

        @Test
        @DisplayName("이미 좋아요 한 상품에 대해 다시 좋아요를 시도하면 중복되지 않는다 (멱등성)")
        void shouldNotAddDuplicateLike() {
            // Arrange
            UserId userId = new UserId("user1");
            Long productId = 1L;
            likeService.addLike(userId, productId); // 첫 번째 좋아요 등록

            // Act
            likeService.addLike(userId, productId); // 중복 좋아요 시도

            // Assert
            long likesCount = ((FakeLikeRepository) likeRepository).likes.size();
            assertThat(likesCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("좋아요 취소 기능 테스트")
    class RemoveLike {

        @Test
        @DisplayName("좋아요를 성공적으로 취소한다")
        void shouldRemoveLikeSuccessfully() {
            // Arrange
            UserId userId = new UserId("user1");
            Long productId = 1L;
            likeService.addLike(userId, productId);
            assertTrue(likeRepository.findByUserIdAndProductId(userId, productId).isPresent());

            // Act
            likeService.removeLike(userId, productId);

            // Assert
            assertFalse(likeRepository.findByUserIdAndProductId(userId, productId).isPresent());
        }

        @Test
        @DisplayName("좋아요를 하지 않은 상품에 대해 취소를 시도해도 예외가 발생하지 않는다")
        void shouldNotFailWhenRemovingNonexistentLike() {
            // Arrange
            UserId userId = new UserId("user1");
            Long productId = 1L;
            assertFalse(likeRepository.findByUserIdAndProductId(userId, productId).isPresent());

            // Act & Assert
            assertDoesNotThrow(() -> likeService.removeLike(userId, productId));
        }
    }

    @Nested
    @DisplayName("좋아요 수 조회 기능 테스트")
    class GetLikesCount {

        @Test
        @DisplayName("특정 상품의 좋아요 수를 정확하게 조회한다.")
        void shouldReturnCorrectLikesCount() {
            // Arrange
            Long productIdA = 1L;
            Long productIdB = 2L;

            likeService.addLike(new UserId("user1"), productIdA);
            likeService.addLike(new UserId("user2"), productIdA);
            likeService.addLike(new UserId("user3"), productIdB);

            // Act
            long countA = likeService.getLikesCount(productIdA);
            long countB = likeService.getLikesCount(productIdB);

            // Assert
            assertThat(countA).isEqualTo(2);
            assertThat(countB).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Like 엔티티 생성 시 유효성 검사 테스트")
    class LikeEntityValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("유효하지 않은 userId로 Like 엔티티 생성 시 실패한다")
        void failToCreateLike_whenUserIdIsInvalid(String invalidUserId) {
            // Arrange
            Long productId = 1L;

            // Act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Like(invalidUserId, productId);
            });

            // Assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("사용자 ID");
        }

        @Test
        @DisplayName("null인 productId로 Like 엔티티 생성 시 실패한다")
        void failToCreateLike_whenProductIdIsNull() {
            // Arrange
            String userId = "user1";
            Long invalidProductId = null;

            // Act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Like(userId, invalidProductId);
            });

            // Assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("상품 ID");
        }
    }
}
