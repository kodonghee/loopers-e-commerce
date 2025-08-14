package com.loopers.application.like;

import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.ProductLikeSummary;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.like.ProductLikeSummaryJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LikeFacadeIntegrationTest {
    @Autowired
    LikeFacade likeFacade;

    @Autowired
    ProductFacade productFacade;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductLikeSummaryJpaRepository productLikeSummaryJpaRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    BrandJpaRepository brandJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String USER_ID = "Annie";
    private Long brandId;

    private Long firstProductId;

    private Long secondProductId;

    @BeforeEach
    void setUp() {
        User firstUser = new User(USER_ID, Gender.F, "1995-06-11", "test@naver.com");
        userRepository.save(firstUser);

        Brand brand = brandJpaRepository.save(new Brand("Gucci"));
        this.brandId = brand.getId();

        Product firstProduct = productFacade.create(
                new ProductCriteria("코트", 10, new BigDecimal("100000"), brandId)
        );

        Product secondProduct = productFacade.create(
                new ProductCriteria("블라우스", 10, new BigDecimal("300000"), brandId)
        );

        this.firstProductId = firstProduct.getId();
        this.secondProductId = secondProduct.getId();

        productLikeSummaryJpaRepository.save(new ProductLikeSummary(firstProductId, brandId));
        productLikeSummaryJpaRepository.save(new ProductLikeSummary(secondProductId, brandId));
    }
    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("좋아요 기능 통합 테스트")
    @Nested
    class LikeTest {
        @DisplayName("사용자가 특정 상품에 좋아요를 처음 누르면 좋아요가 저장된다.")
        @Test
        void shouldAddLike_whenNotExists() {
            // arrange
            LikeCriteria criteria = new LikeCriteria(USER_ID, firstProductId);

            // act
            likeFacade.likeProduct(criteria);

            // assert
            List<Like> likes = likeRepository.findAllByUserId(new UserId(USER_ID));
            assertThat(likes).hasSize(1);
            assertThat(likes.get(0).getUserId()).isEqualTo(USER_ID);
            assertThat(likes.get(0).getProductId()).isEqualTo(firstProductId);
        }

        @DisplayName("사용자가 특정 상품에 좋아요를 두번째 누르면 아무 일도 일어나지 않는다.")
        @Test
        void shouldNotAddDuplicateLike() {
            // arrange
            LikeCriteria criteria = new LikeCriteria(USER_ID, firstProductId);
            likeFacade.likeProduct(criteria);

            // act
            likeFacade.likeProduct(criteria);

            // assert
            List<Like> likes = likeRepository.findAllByUserId(new UserId(USER_ID));
            assertThat(likes).hasSize(1);
            assertThat(likes.get(0).getUserId()).isEqualTo(USER_ID);
            assertThat(likes.get(0).getProductId()).isEqualTo(firstProductId);
        }

        @DisplayName("사용자는 기존에 좋아요를 누른 상품에 좋아요 취소를 할 수 있다.")
        @Test
        void shouldRemoveLike_whenLikeExists() {
            // arrange
            LikeCriteria criteria = new LikeCriteria(USER_ID, firstProductId);
            likeFacade.likeProduct(criteria);

            // act
            likeFacade.cancelLikeProduct(criteria);

            // assert
            List<Like> likes = likeRepository.findAllByUserId(new UserId(USER_ID));
            assertThat(likes).isEmpty();
        }

        @DisplayName("사용자가 특정 상품에 좋아요를 누르지 않은 상태에서 취소 요청 시 아무 일도 일어나지 않는다.")
        @Test
        void shouldDoNothing_whenRemoveNotExistLike() {
            // arrange
            LikeCriteria criteria = new LikeCriteria(USER_ID, firstProductId);

            // act
            likeFacade.cancelLikeProduct(criteria);

            // assert
            List<Like> likes = likeRepository.findAllByUserId(new UserId(USER_ID));
            assertThat(likes).isEmpty();
        }
    }

    @DisplayName("좋아요 조회 통합 테스트")
    @Nested
    class GetLikeCountTest {
        @DisplayName("사용자는 자신이 좋아요한 상품 목록을 조회할 수 있다.")
        @Test
        void shouldReturnLikedProductsForUser() {
            // arrange
            likeFacade.likeProduct(new LikeCriteria(USER_ID, firstProductId));
            likeFacade.likeProduct(new LikeCriteria(USER_ID, secondProductId));

            // act
            List<LikeResult> likedProducts = likeFacade.getLikedProducts(new UserId(USER_ID));

            // assert
            assertThat(likedProducts).hasSize(2);
            List<Long> productIds = likedProducts.stream()
                    .map(LikeResult::productId)
                    .toList();

            assertThat(productIds).containsExactlyInAnyOrder(firstProductId, secondProductId);
        }

        @DisplayName("특정 상품에 대한 총 좋아요 수를 조회할 수 있다.")
        @Test
        void shouldReturnCorrectLikeCount() {
            // arrange
            likeFacade.likeProduct(new LikeCriteria(USER_ID, firstProductId));
            likeFacade.likeProduct(new LikeCriteria("Bailey", firstProductId));
            likeFacade.likeProduct(new LikeCriteria("Woochan", firstProductId));

            // act
            Long likeCount = likeFacade.getLikesCount(firstProductId);

            // assert
            assertThat(likeCount).isEqualTo(3L);
        }
    }

}
