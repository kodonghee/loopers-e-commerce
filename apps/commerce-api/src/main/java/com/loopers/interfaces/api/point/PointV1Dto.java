package com.loopers.interfaces.api.point;

import java.math.BigDecimal;

public class PointV1Dto {
    public record PointChargeRequest(BigDecimal amount) {
    }

    public record PointResponse(BigDecimal pointValue) {
        public static PointResponse from(BigDecimal pointValue) {
            return new PointResponse(pointValue);
        }
    }

}
