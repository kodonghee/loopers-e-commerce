package com.loopers.application.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.ProductLikeSummary;
import com.loopers.domain.like.ProductLikeSummaryRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import java.util.List;

import static com.loopers.application.like.LikeMapper.toLike;

@Slf4j
@RequiredArgsConstructor
@Service
public class LikeFacade {

    private final LikeRepository likeRepository;
    private final ProductLikeSummaryRepository productLikeSummaryRepository;

    @Transactional(rollbackFor = Exception.class)
    public void likeProduct(LikeCriteria criteria) {
        Like like = toLike(criteria);
        if (likeRepository.findByUserIdAndProductId(new UserId(like.getUserId()), like.getProductId()).isPresent()) {
            return;
        }

        try {
            likeRepository.save(like);
        } catch (DataIntegrityViolationException e) {
            return;
        }

        ProductLikeSummary summary = productLikeSummaryRepository.findByProductIdForUpdate(like.getProductId())
                .orElseThrow(() -> new IllegalStateException("Summary row가 없습니다. 사전 생성 필요합니다."));

        summary.increment();
        productLikeSummaryRepository.save(summary);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelLikeProduct(LikeCriteria criteria) {
        UserId userId = new UserId(criteria.userId());
        Long productId = criteria.productId();

        int deleted = likeRepository.deleteByUserIdAndProductId(userId, productId);
        if (deleted == 0) {
            return;
        }

        ProductLikeSummary summary = productLikeSummaryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new IllegalStateException("Summary row가 없습니다. 사전 생성 필요합니다."));

        summary.decrement();
        productLikeSummaryRepository.save(summary);
    }


    @Transactional(readOnly = true)
    public List<LikeResult> getLikedProducts(UserId userId) {
        return likeRepository.findAllByUserId(userId).stream()
                .map(like -> new LikeResult(like.getUserId(), like.getProductId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getLikesCount(Long productId) {
        return productLikeSummaryRepository.findByProductId(productId)
                .map(ProductLikeSummary::getLikeCount)
                .orElse(0L);
    }
}
