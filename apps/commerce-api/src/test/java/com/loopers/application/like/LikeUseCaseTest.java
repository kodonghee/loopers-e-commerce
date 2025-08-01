package com.loopers.application.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeDomainService;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikeUseCase 단위 테스트")
class LikeUseCaseTest {

    // 테스트 대상인 UseCase 객체에 Mock 객체들을 주입
    @InjectMocks
    private LikeUseCase likeUseCase;

    // UseCase가 의존하는 LikeDomainService를 Mock 객체로 생성
    @Mock
    private LikeDomainService likeDomainService;

    // UseCase가 직접 사용하는 LikeRepository를 Mock 객체로 생성
    @Mock
    private LikeRepository likeRepository;

    private static final String USER_ID_STRING = "testUser1";
    private static final UserId USER_ID = new UserId(USER_ID_STRING);
    private static final Long PRODUCT_ID = 1L;
    private static final Long OTHER_PRODUCT_ID = 2L;

    @Nested
    @DisplayName("likeProduct 메서드 테스트")
    class LikeProductTest {
        @Test
        @DisplayName("좋아요 요청 시, LikeDomainService의 addLike가 호출되어야 한다.")
        void likeProduct_shouldCallAddLike() {
            // arrange

            // act
            likeUseCase.likeProduct(USER_ID, PRODUCT_ID);

            // assert
            verify(likeDomainService).addLike(USER_ID, PRODUCT_ID);
        }
    }

    @Nested
    @DisplayName("cancelLikeProduct 메서드 테스트")
    class CancelLikeProductTest {
        @Test
        @DisplayName("좋아요 취소 요청 시, LikeDomainService의 removeLike가 호출되어야 한다.")
        void cancelLikeProduct_shouldCallRemoveLike() {
            // arrange
            // LikeUseCase의 cancelLikeProduct 메서드가 LikeDomainService의 removeLike를 호출하는지 테스트
            // 특별한 사전 설정은 필요하지 않습니다.

            // act
            likeUseCase.cancelLikeProduct(USER_ID, PRODUCT_ID);

            // assert
            verify(likeDomainService).removeLike(USER_ID, PRODUCT_ID);
        }
    }

    @Nested
    @DisplayName("getLikedProducts 메서드 테스트")
    class GetLikedProductsTest {
        @Test
        @DisplayName("좋아요한 상품 목록 조회 시, LikeInfo 리스트를 반환해야 한다.")
        void getLikedProducts_shouldReturnLikeInfoList() {
            // arrange
            List<Like> mockLikes = List.of(
                    new Like(USER_ID_STRING, PRODUCT_ID),
                    new Like(USER_ID_STRING, OTHER_PRODUCT_ID)
            );
            when(likeRepository.findAllByUserId(USER_ID)).thenReturn(mockLikes);

            List<LikeInfo> expectedList = List.of(
                    new LikeInfo(USER_ID_STRING, PRODUCT_ID),
                    new LikeInfo(USER_ID_STRING, OTHER_PRODUCT_ID)
            );

            // act
            List<LikeInfo> result = likeUseCase.getLikedProducts(USER_ID);

            // assert
            assertThat(result).containsExactlyInAnyOrderElementsOf(expectedList);
        }
    }
}
