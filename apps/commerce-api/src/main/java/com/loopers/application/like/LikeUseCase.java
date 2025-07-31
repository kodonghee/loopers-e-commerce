package com.loopers.application.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LikeUseCase {

    private final LikeRepository likeRepository;

    @Transactional
    public void likeProduct(String userId, Long productId) {
        likeRepository.findByUserIdAndProductId(userId, productId)
                .ifPresentOrElse(
                        like -> {},
                        () -> likeRepository.save(new Like(userId, productId))
                );
    }

    @Transactional
    public void cancelLikeProduct(String userId, Long productId) {
        likeRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(likeRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<LikeInfo> getLikedProducts(String userId) {
        return likeRepository.findAllByUserId(userId).stream()
                .map(like -> new LikeInfo(like.getUserId(), like.getProductId()))
                .toList();
    }
}
