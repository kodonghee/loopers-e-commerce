package com.loopers.application.product;

import java.math.BigDecimal;

public record ProductCriteria(
        String name,
        int stockValue,
        BigDecimal priceValue,
        Long brandId
) {}
