package com.loopers.application.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.ProductLikeSummary;
import com.loopers.domain.like.ProductLikeSummaryRepository;
import com.loopers.domain.like.event.LikeCancelledEvent;
import com.loopers.domain.like.event.LikeCreatedEvent;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.loopers.application.like.LikeMapper.toLike;
import static com.loopers.config.redis.RedisCacheConfig.CACHE_PRODUCT_DETAIL;
import static com.loopers.config.redis.RedisCacheConfig.CACHE_PRODUCT_LIST;

@Slf4j
@RequiredArgsConstructor
@Service
public class LikeFacade {

    private final LikeRepository likeRepository;
    private final ProductLikeSummaryRepository productLikeSummaryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_PRODUCT_DETAIL, key = "#criteria.productId()", condition = "#result == true"),
            @CacheEvict(cacheNames = CACHE_PRODUCT_LIST,   allEntries = true,           condition = "#result == true")
    })
    @Transactional(rollbackFor = Exception.class)
    public boolean likeProduct(LikeCriteria criteria) {
        Like like = toLike(criteria);
        if (likeRepository.findByUserIdAndProductId(new UserId(like.getUserId()), like.getProductId()).isPresent()) {
            return false;
        }

        try {
            likeRepository.save(like);
            eventPublisher.publishEvent(LikeCreatedEvent.of(like.getUserId(), like.getProductId()));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_PRODUCT_DETAIL, key = "#criteria.productId()", condition = "#result == true"),
            @CacheEvict(cacheNames = CACHE_PRODUCT_LIST,   allEntries = true,           condition = "#result == true")
    })
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelLikeProduct(LikeCriteria criteria) {
        UserId userId = new UserId(criteria.userId());
        Long productId = criteria.productId();

        int deleted = likeRepository.deleteByUserIdAndProductId(userId, productId);
        if (deleted == 0) {
            return false;
        }

        eventPublisher.publishEvent(LikeCancelledEvent.of(userId.getUserId(), productId));
        return true;
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
