package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    Page<Product> findAllByBrandId(Long brandId, Pageable pageable);

}
