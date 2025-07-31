package com.loopers.application.product;

import com.loopers.domain.brand.BrandReader;
import com.loopers.domain.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductUseCase {

    private final ProductRepository productRepository;
    private final BrandReader brandReader;
    private final LikeCountReader likeCountReader;

    @Transactional
    public Long create(ProductCommand command) {

        Product product = new Product(
                command.name(),
                new Stock(command.stockValue()),
                new Money(command.priceValue()),
                command.brandId()
        );
        productRepository.save(product);
        return product.getId();
    }

    @Transactional(readOnly = true)
    public List<ProductInfo> getProductList(ProductSearchCondition condition) {
        List<Product> products = productRepository.findAllByCondition(condition);

        return products.stream()
                .map(this::toInfo)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductInfo getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        String brandName = brandReader.getBrandName(product.getBrandId());
        int likeCount = likeCountReader.getLikeCountByProductId(product.getId());

        return new ProductInfo(
                product.getId(),
                product.getName(),
                product.getStock().getValue(),
                product.getPrice().getAmount(),
                brandName,
                likeCount
        );
    }

    private ProductInfo toInfo(Product p){
        return new ProductInfo(
                p.getId(),
                p.getName(),
                p.getStock().getValue(),
                p.getPrice().getAmount(),
                brandReader.getBrandName(p.getBrandId()),
                likeCountReader.getLikeCountByProductId(p.getId())
        );
    }
}
