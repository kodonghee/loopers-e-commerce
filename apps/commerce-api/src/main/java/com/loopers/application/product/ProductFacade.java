package com.loopers.application.product;

import com.loopers.domain.brand.BrandReader;
import com.loopers.domain.like.LikeCountReader;
import com.loopers.domain.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductFacade {

    private final ProductRepository productRepository;
    private final BrandReader brandReader;
    private final LikeCountReader likeCountReader;

    @Transactional
    public Product create(ProductCriteria command) {

        Product product = new Product(
                command.name(),
                new Stock(command.stockValue()),
                new Money(command.priceValue()),
                command.brandId()
        );
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResult> getProductList(ProductSearchCondition condition) {
        List<Product> products = productRepository.findAllByCondition(condition);

        return products.stream()
                .map(this::toInfo)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResult getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        String brandName = brandReader.getBrandName(product.getBrandId());
        int likeCount = likeCountReader.getLikeCountByProductId(product.getId());
        // 여기에 productDomainService 두는 것의 장점? 리팩토링 때 반영하기
        return new ProductResult(
                product.getId(),
                product.getName(),
                product.getStock().getValue(),
                product.getPrice().getAmount(),
                brandName,
                likeCount
        );
    }

    private ProductResult toInfo(Product p){
        return new ProductResult(
                p.getId(),
                p.getName(),
                p.getStock().getValue(),
                p.getPrice().getAmount(),
                brandReader.getBrandName(p.getBrandId()),
                likeCountReader.getLikeCountByProductId(p.getId())
        );
    }
}
