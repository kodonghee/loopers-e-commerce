package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeCountReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeCountReaderImpl implements LikeCountReader {

    @Override
    public int getLikeCountByProductId(Long productId) {
        // TODO: 좋아요 수 조회 구현
        return 0;
    }
}
