package com.loopers.application.product;

import java.math.BigDecimal;

public record ProductInfo(Long productId, String name, int stock, BigDecimal price, String brandName, int likeCount) {

}

