package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<User> find(UserId userId);
    User save(User user);
    boolean existsByUserId(String userId);
}
