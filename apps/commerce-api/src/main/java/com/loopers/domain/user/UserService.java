package com.loopers.domain.user;

import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.user.UserV1Dto;
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
    public User getUser(String userId) {
        return userRepository.find(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "[id = " + userId + "] 회원을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public UserInfo getUserInfo(String userId) {
        User user = getUser(userId);
        return UserInfo.from(user);
    }

    public User signUp(UserV1Dto.UserRequest request) {
        if (userRepository.existsByUserId(request.userId())) {
            throw new CoreException(ErrorType.CONFLICT, "이미 가입된 ID 입니다.");
        }
        User user = new User(
                request.userId(),
                request.gender(),
                request.birthDate(),
                request.email()
        );
        return userRepository.save(user);
    }
}
