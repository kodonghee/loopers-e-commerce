package com.loopers.domain.product;

import java.math.BigDecimal;

public interface ProductListView {
    Long getId();
    String getName();
    Integer getStockValue();
    BigDecimal getPrice();
    String getBrandName();
    Long getLikeCount();
}
