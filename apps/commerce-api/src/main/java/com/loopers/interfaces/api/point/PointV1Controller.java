package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointFacade;
import com.loopers.domain.user.UserId;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.annotation.UserIdParam;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller implements PointV1ApiSpec {

    private final PointFacade pointFacade;

    @GetMapping
    @Override
    public ApiResponse<PointV1Dto.PointResponse> getPoints(
            @UserIdParam UserId userId
    ) {
        BigDecimal points = pointFacade.getPoints(userId);
        if (points == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 ID입니다.");
        }
        return ApiResponse.success(PointV1Dto.PointResponse.from(points));
    }

    @PostMapping("/charge")
    @Override
    public ApiResponse<PointV1Dto.PointResponse> chargePoint(
            @UserIdParam UserId userId,
            @RequestBody PointV1Dto.PointChargeRequest request
    ) {
        BigDecimal points = pointFacade.chargePoints(userId, request.amount());
        return ApiResponse.success(PointV1Dto.PointResponse.from(points));
    }
}
