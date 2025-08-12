package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductListView;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public List<Product> findByIdForUpdate(Collection<Long> ids) {
        return productJpaRepository.findByIdForUpdate(ids);
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll();
    }

    @Override
    public List<Product> findAllByCondition(ProductSearchCondition condition) {
        Pageable pageable = condition.getPageable();

        if (condition.getBrandId() != null) {
            return productJpaRepository.findAllByBrandId(condition.getBrandId(), pageable).getContent();
        } else {
            return productJpaRepository.findAll(pageable).getContent();
        }
    }

    @Override
    public List<Product> findAllById(List<Long> ids) {
        return productJpaRepository.findAllById(ids);
    }

    @Override
    public List<ProductListView> findListViewByCondition(ProductSearchCondition condition) {
        int limit  = condition.getSize();
        int offset = condition.getPage() * condition.getSize();

        return switch (condition.getSortType()) {
            case LIKES_DESC -> productJpaRepository.findListLikesDesc(condition.getBrandId(), limit, offset);
            case PRICE_ASC  -> productJpaRepository.findListPriceAsc(condition.getBrandId(), limit, offset);
            case LATEST     -> productJpaRepository.findListLatest(condition.getBrandId(), limit, offset);
        };
    }

}
