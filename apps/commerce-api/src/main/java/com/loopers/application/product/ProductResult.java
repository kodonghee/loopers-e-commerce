package com.loopers.application.product;

import java.math.BigDecimal;

public record ProductResult(Long productId, String name, int stock, BigDecimal price, String brandName, int likeCount) {

}

