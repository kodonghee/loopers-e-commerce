package com.loopers.domain.product;

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
}
