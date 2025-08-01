package com.loopers.domain.like;

import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LikeDomainService {

    private final LikeRepository likeRepository;

    @Transactional
    public void addLike(UserId userId, Long productId) {
        if (likeRepository.findByUserIdAndProductId(userId, productId).isEmpty()) {
            Like like = new Like(userId.getUserId(), productId);
            likeRepository.save(like);
        }
    }

    @Transactional
    public void removeLike(UserId userId, Long productId) {
        likeRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(likeRepository::delete);
    }

    @Transactional(readOnly = true)
    public long getLikesCount(Long productId) {
        return likeRepository.countByProductId(productId);
    }
}
