package com.loopers.domain.product;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class ProductSearchCondition {

    private final Long brandId;
    private final ProductSortType sortType;
    private final int page;
    private final int size;

    public ProductSearchCondition(Long brandId, ProductSortType sortType, int page, int size) {
        this.brandId = brandId;
        this.sortType = sortType;
        this.page = page;
        this.size = size;
    }

    public Long getBrandId() {
        return brandId;
    }

    public ProductSortType getSortType() {
        return sortType;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public enum ProductSortType {
        LATEST,
        PRICE_ASC,
        LIKES_DESC
    }

    public Pageable getPageable (){
        if (sortType == ProductSortType.LIKES_DESC) {
            return PageRequest.of(this.page, this.size);
        }
        return PageRequest.of(
                this.page,
                this.size,
                convertSort(this.sortType)
        );
    }

    private Sort convertSort(ProductSearchCondition.ProductSortType sortType) {
        return switch (sortType) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case LIKES_DESC -> Sort.unsorted();
        };
    }
}
