package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInfo getUserInfo(String userId) {
        return userRepository.find(userId)
                .map(UserInfo::from)
                .orElse(null);
    }

    @Transactional
    public UserInfo signUp(UserCommand.Create command) {
        if (userRepository.existsByUserId(command.userId())) {
            throw new CoreException(ErrorType.CONFLICT, "이미 가입된 ID 입니다.");
        }

        if (command.gender() == null || command.gender().isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별이 없습니다.");
        }
        User user = new User(
                command.userId(),
                command.gender(),
                command.birthDate(),
                command.email()
        );

        User saved = userRepository.save(user);
        return UserInfo.from(saved);
    }

    @Transactional(readOnly = true)
    public Long getPoints(String userId) {
        return userRepository.find(userId)
                .map(User::getPoint)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Long chargePoints(String userId, Long amount) {
        return userRepository.find(userId)
                .map(user -> {
                    user.addPoint(amount);
                    return user.getPoint();
                })
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "해당 ID의 회원이 없습니다."));
    }
}
