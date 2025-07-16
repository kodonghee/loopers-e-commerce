package com.loopers.domain.user;

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
