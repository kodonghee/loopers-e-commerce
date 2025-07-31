package com.loopers.domain.like;

import com.loopers.domain.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeTest {
    @DisplayName("사용자가 좋아요를 누르면 사용자 ID와 상품 ID가 저장된다.")
    @Test
    void createLikeInfo_whenUserLikeProduct() {
        // arrange
        UserId userId = new UserId("gdh5866");
        Long productId = 10L;

        // act
        Like like = new Like(userId.getUserId(), productId);

        // assert
        assertThat(like.getUserId()).isEqualTo(userId.getUserId());
        assertThat(like.getProductId()).isEqualTo(productId);
    }

    @DisplayName("사용자가 동일한 상품을 좋아요 누르면 좋아요 데이터는 이전 데이터와 동일하다.")
    @Test
    void equalsLike_whenSameUserAndProduct() {
        // arrange
        UserId userId = new UserId("donghee");
        Long productId = 1L;

        Like like1 = new Like(userId.getUserId(), productId);
        Like like2 = new Like(userId.getUserId(), productId);

        // act & assert
        assertThat(like1).isEqualTo(like2);
    }
}
