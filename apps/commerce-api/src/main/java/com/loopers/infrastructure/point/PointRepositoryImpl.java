package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository pointJpaRepository;

    @Override
    public Optional<Point> find(UserId userId) {
        return pointJpaRepository.findByUserId(userId.getUserId());
    }
    @Override
    public Optional<Point> findByUserId(String userId) {
        return pointJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<Point> findByUserIdForUpdate(String userId) {
        return pointJpaRepository.findByUserIdForUpdate(userId);
    }

    @Override
    public Point save(Point point) {
        return pointJpaRepository.save(point);
    }

    @Override
    public boolean existsByUserId(UserId userId) {
        return pointJpaRepository.existsByUserId(userId.getUserId());
    }
}
