package com.loopers.application.user;

import com.loopers.domain.user.User;

public record UserInfo(String userId, String gender, String birthDate, String email) {
    public static UserInfo from(User user) {
        return new UserInfo(
                user.getUserId(),
                user.getGender(),
                user.getBirthDate(),
                user.getEmail()
        );
    }
}
