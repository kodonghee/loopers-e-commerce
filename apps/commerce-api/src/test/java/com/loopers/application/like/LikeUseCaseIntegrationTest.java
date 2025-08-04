package com.loopers.application.like;

import com.loopers.domain.like.Like;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LikeUseCaseIntegrationTest {
    @DisplayName("상품 좋아요 등록에 성공 한다.")
    @Test
    void createLikeSuccessfully() {
        // arrange
        String userId = "user123";
        Long productId = 1L;

        // act
        Like like = new Like(userId, productId);

        // assert
        assertThat(like).isNotNull();
        assertThat(like.getUserId()).isEqualTo(userId);
        assertThat(like.getProductId()).isEqualTo(productId);
    }
}
