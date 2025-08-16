package com.loopers.application.like;

import com.loopers.domain.like.ProductLikeSummary;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.infrastructure.like.ProductLikeSummaryJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("좋아요 동시성 테스트")
class LikeFacadeConcurrencyIntegrationTest {

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private LikeJpaRepository likeJpaRepository;

    @Autowired
    private ProductLikeSummaryJpaRepository productLikeSummaryJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private final Long productId = 1L;
    private final Long brandId = 1L;

    @BeforeEach
    void setUp() {
        likeJpaRepository.deleteAll();
        productLikeSummaryJpaRepository.deleteAll();

        productLikeSummaryJpaRepository.save(new ProductLikeSummary(productId, brandId));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("사용자가 특정 상품에 좋아요를 동시에 눌러도 최종 좋아요 수가 정확하다. (비관적 락)")
    @Test
    void likeIncreaseCorrectly_whenConcurrentRequests() throws InterruptedException {
        int numberOfUsers = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfUsers);

        for (int i = 0; i < numberOfUsers; i++) {
            int userNum = i;
            executor.submit(() -> {
                try {
                    String userId = "user-" + userNum;
                    likeFacade.likeProduct(new LikeCriteria(userId, productId));
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 작업 완료 대기
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).as("동시에 보낸 작업이 시간 내에 완료 되어야 함").isTrue();

        Long summaryCount = productLikeSummaryJpaRepository.findById(productId)
                .map(ProductLikeSummary::getLikeCount)
                .orElse(0L);
        assertThat(summaryCount).isEqualTo(numberOfUsers);

        Long likeRows = likeJpaRepository.count();
        assertThat(likeRows).isEqualTo(numberOfUsers);

    }

    @DisplayName("사용자가 특정 상품에 동시에 싫어요 요청을 해도 좋아요 수가 정확히 감소한다. (비관적 락)")
    @Test
    void likeDecreaseCorrectly_whenConcurrentRequests() throws Exception {
        int users = 50;

        for (int i = 0; i < users; i++) {
            likeFacade.likeProduct(new LikeCriteria("u-" + i, productId));
        }

        ExecutorService es = Executors.newFixedThreadPool(16);
        CountDownLatch latch = new CountDownLatch(users);
        for (int i = 0; i < users; i++) {
            int idx = i;
            es.submit(() -> {
                try { likeFacade.cancelLikeProduct(new LikeCriteria("u-"+idx, productId)); }
                finally { latch.countDown(); }
            });
        }
        latch.await(30, TimeUnit.SECONDS);
        es.shutdown();

        assertThat(likeFacade.getLikesCount(productId)).isEqualTo(0L);
    }

    @DisplayName("좋아요와 싫어요가 섞여도 최종 좋아요 수가 정확하다.")
    @Test
    void mixedLikeAndCancel() throws Exception {
        int total = 100;
        ExecutorService es = Executors.newFixedThreadPool(16);
        CountDownLatch latch = new CountDownLatch(total);

        for (int i = 0; i < total; i++) {
            int idx = i;
            es.submit(() -> {
                try {
                    String uid = "u-" + idx;
                    likeFacade.likeProduct(new LikeCriteria(uid, productId));
                    if (idx % 2 == 1) {
                        likeFacade.cancelLikeProduct(new LikeCriteria(uid, productId));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(30, TimeUnit.SECONDS);
        es.shutdown();

        assertThat(likeFacade.getLikesCount(productId)).isEqualTo(total / 2);
    }

}
