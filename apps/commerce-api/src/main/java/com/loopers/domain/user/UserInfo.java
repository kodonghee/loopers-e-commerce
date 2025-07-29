package com.loopers.domain.user;

public record UserInfo(String userId, String gender, String birthDate, String email) {
    // record로 객체 선언 시, get 메소드는 자동으로 생성됨
    public static UserInfo from(User user) {
        return new UserInfo(
                user.getUserId(),
                user.getGender().name(),
                user.getBirthDate(),
                user.getEmail()
        );
    }
}
