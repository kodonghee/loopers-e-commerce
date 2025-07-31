package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PointUseCase {

    private final PointRepository pointRepository;

    @Transactional(readOnly = true)
    public Long getPoints(UserId userId) {
        return pointRepository.find(userId)
                .map(Point::getPointValue)
                .orElse(null);
    }

    @Transactional
    public Long chargePoints(UserId userId, Long amount) {
        return pointRepository.find(userId)
                .map(point -> {
                    point.charge(amount);
                    return point.getPointValue();
                })
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "해당 ID의 회원이 없습니다."));
    }

    @Transactional
    public void initializePoint(UserId userId) {
        pointRepository.save(new Point(userId.getUserId(), 0L));
    }
}

