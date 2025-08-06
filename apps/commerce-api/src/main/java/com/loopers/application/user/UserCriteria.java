package com.loopers.application.user;

import com.loopers.domain.user.Gender;

public class UserCriteria {

    public record Create (String userId, Gender gender, String birthDate, String email) {

    }

}
