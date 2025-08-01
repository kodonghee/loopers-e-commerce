package com.loopers.interfaces.api.brand;

public class BrandV1Dto {

    public record BrandResponse(Long id, String name) {
        public static BrandResponse from(Long id, String name) {
            return new BrandResponse(id, name);
        }
    }

}
