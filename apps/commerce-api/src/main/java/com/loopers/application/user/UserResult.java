package com.loopers.application.user;

import com.loopers.domain.user.User;

public record UserResult(String userId, String gender, String birthDate, String email) {
    // record로 객체 선언 시, get 메소드는 자동으로 생성됨
    public static UserResult from(User user) {
        return new UserResult(
                user.getUserId(),
                user.getGender().name(),
                user.getBirthDate(),
                user.getEmail()
        );
    }
}
