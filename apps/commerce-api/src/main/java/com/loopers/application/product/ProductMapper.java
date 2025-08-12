package com.loopers.application.product;

import com.loopers.domain.product.Money;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductListView;
import com.loopers.domain.product.Stock;

public class ProductMapper {
    public static Product toProduct(ProductCriteria criteria) {
        return new Product(criteria.name(), new Stock(criteria.stockValue()), new Money(criteria.priceValue()), criteria.brandId());
    }

    public static ProductResult fromProduct(Product product, String brandName, Long likeCount) {
        return new ProductResult(product.getId(),
                product.getName(),
                product.getStock().getValue(),
                product.getPrice().getAmount(),
                brandName,
                likeCount);
    }

    public static ProductResult from(ProductListView v) {
        return new ProductResult(
                v.getId(),
                v.getName(),
                v.getStockValue(),
                v.getPrice(),
                v.getBrandName(),
                v.getLikeCount()
        );
    }
}
