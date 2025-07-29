package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> find(UserId userId) {
        return userJpaRepository.findByUserId(userId.getUserId());
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return userJpaRepository.existsByUserId(userId);
    }
}
