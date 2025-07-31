package com.loopers.interfaces.api.point;

public class PointV1Dto {
    public record PointChargeRequest(Long amount) {
    }

    public record PointResponse(Long pointValue) {
        public static PointResponse from(Long pointValue) {
            return new PointResponse(pointValue);
        }
    }

}
