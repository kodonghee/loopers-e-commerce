package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "brand")
public class Brand extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    protected Brand() {}

    public Brand(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("브랜드명은 필수입니다.");
        }
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
