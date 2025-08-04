package com.loopers.application.like;

import com.loopers.domain.like.LikeDomainService;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LikeUseCase {

    private final LikeDomainService likeDomainService;

    private final LikeRepository likeRepository;

    @Transactional
    public void likeProduct(UserId userId, Long productId) {
        likeDomainService.addLike(userId, productId);
    }

    @Transactional
    public void cancelLikeProduct(UserId userId, Long productId) {
        likeDomainService.removeLike(userId, productId);
    }

    @Transactional(readOnly = true)
    public List<LikeInfo> getLikedProducts(UserId userId) {
        return likeRepository.findAllByUserId(userId).stream()
                .map(like -> new LikeInfo(like.getUserId(), like.getProductId()))
                .toList();
    }
}
