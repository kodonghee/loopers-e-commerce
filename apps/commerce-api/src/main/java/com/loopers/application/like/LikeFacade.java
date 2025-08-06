package com.loopers.application.like;

import com.loopers.domain.like.LikeService;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LikeFacade {

    private final LikeService likeService;

    @Transactional
    public void likeProduct(UserId userId, Long productId) {
        likeService.addLike(userId, productId);
    }

    @Transactional
    public void cancelLikeProduct(UserId userId, Long productId) {
        likeService.removeLike(userId, productId);
    }

    @Transactional(readOnly = true)
    public List<LikeResult> getLikedProducts(UserId userId) {
        return likeService.getAllByUserId(userId).stream()
                .map(like -> new LikeResult(like.getUserId(), like.getProductId()))
                .toList();
    }
}
