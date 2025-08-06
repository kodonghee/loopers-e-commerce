package com.loopers.domain.like;

import com.loopers.domain.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LikeService {

    private final LikeRepository likeRepository;

    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

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

    @Transactional(readOnly = true)
    public List<Like> getAllByUserId(UserId userId) {
        return likeRepository.findAllByUserId(userId);
    }
}
