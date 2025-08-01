package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeCountReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeCountCountReaderImpl implements LikeCountReader {

    private final LikeJpaRepository likeJpaRepository;

    @Override
    public int getLikeCountByProductId(Long productId) {
        return (int) likeJpaRepository.countByProductId(productId);
    }
}
