package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserCriteria;
import com.loopers.application.user.UserResult;
import com.loopers.domain.user.Gender;

public class UserV1Dto {
    public record UserRequest(String userId, String gender, String birthDate, String email) {
        public UserCriteria.Create toCommand (){
            return new UserCriteria.Create(userId, Gender.from(gender), birthDate, email);
        }
    }
    public record UserResponse(String userId, String gender, String birthDate, String email) {
        public static UserResponse from(UserResult info) {
            return new UserResponse(
                    info.userId(),
                    info.gender(),
                    info.birthDate(),
                    info.email()
            );
        }
    }
}
