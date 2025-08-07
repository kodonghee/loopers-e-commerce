package com.loopers.domain.product;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Optional<Product> findById(Long id);
    List<Product> findByIdForUpdate(Collection<Long> ids);
    Product save(Product product);
    List<Product> findAll();
    List<Product> findAllByCondition(ProductSearchCondition condition);
    List<Product> findAllById(List<Long> ids);
}
