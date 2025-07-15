package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;

public class UserV1Dto {
    public record UserRequest(String userId, String gender, String birthDate, String email) {

    }
    public record UserResponse(String userId, String gender, String birthDate, String email) {
        public static UserResponse from(UserInfo info) {
            return new UserResponse(
                    info.userId(),
                    info.gender(),
                    info.birthDate(),
                    info.email()
            );
        }
    }
}
