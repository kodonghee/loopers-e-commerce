package com.loopers.domain.point;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;

import java.util.Optional;

public interface PointRepository {
    Optional<Point> find(UserId userId);
    Optional<Point> findByUserIdForUpdate(String userId);
    Point save(Point point);
    boolean existsByUserId(UserId userId);

}
