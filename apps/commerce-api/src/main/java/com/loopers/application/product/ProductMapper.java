package com.loopers.application.product;

import com.loopers.domain.brand.BrandReader;
import com.loopers.domain.like.LikeCountReader;
import com.loopers.domain.product.Money;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.Stock;

public class ProductMapper {
    private final BrandReader brandReader;
    private final LikeCountReader likeCountReader;

    public ProductMapper(BrandReader brandReader, LikeCountReader likeCountReader) {
        this.brandReader = brandReader;
        this.likeCountReader = likeCountReader;
    }
    public static Product toProduct(ProductCriteria criteria) {
        return new Product(criteria.name(), new Stock(criteria.stockValue()), new Money(criteria.priceValue()), criteria.brandId());
    }

    public ProductResult fromProduct(Product product) {
        String brandName = brandReader.getBrandName(product.getBrandId());
        int likeCount = likeCountReader.getLikeCountByProductId(product.getId());
        return new ProductResult(product.getId(),
                product.getName(),
                product.getStock().getValue(),
                product.getPrice().getAmount(),
                brandName,
                likeCount);
    }
}
