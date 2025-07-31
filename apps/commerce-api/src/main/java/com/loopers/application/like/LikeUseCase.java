package com.loopers.application.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LikeUseCase {

    private final LikeRepository likeRepository;

    @Transactional
    public void likeProduct(UserId userId, Long productId) {
        likeRepository.findByUserIdAndProductId(userId, productId)
                .ifPresentOrElse(
                        like -> {},
                        () -> likeRepository.save(new Like(userId.getUserId(), productId))
                );
    }

    @Transactional
    public void cancelLikeProduct(UserId userId, Long productId) {
        likeRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(likeRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<LikeInfo> getLikedProducts(UserId userId) {
        return likeRepository.findAllByUserId(userId).stream()
                .map(like -> new LikeInfo(like.getUserId(), like.getProductId()))
                .toList();
    }
}
