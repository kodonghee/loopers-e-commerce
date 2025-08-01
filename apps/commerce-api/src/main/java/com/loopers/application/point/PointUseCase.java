package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserSignedUpEvent;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
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

    @EventListener
    @Transactional
    public void handleUserSignedUpEvent(UserSignedUpEvent event) {
        pointRepository.save(new Point(event.userId().getUserId(), 0L));
    }
}

