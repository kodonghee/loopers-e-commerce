package com.loopers.application.user;

import com.loopers.domain.user.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserUseCase {

    private final UserRepository userRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public UserInfo getUserInfo(UserId userId) {
        return userRepository.find(userId)
                .map(UserInfo::from)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "해당 ID의 회원이 없습니다."));
    }

    @Transactional
    public UserInfo signUp(UserCommand.Create command) {

        if (userRepository.existsByUserId(new UserId(command.userId()))) {
            throw new CoreException(ErrorType.CONFLICT, "이미 가입된 ID 입니다.");
        }

        User user = new User(
                command.userId(),
                command.gender(),
                command.birthDate(),
                command.email()
        );

        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserSignedUpEvent(new UserId(saved.getUserId())));

        return UserInfo.from(saved);
    }
}
