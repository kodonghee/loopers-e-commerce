package com.loopers.application.user;

import com.loopers.domain.user.Gender;

public class UserCommand {

    public record Create (String userId, Gender gender, String birthDate, String email) {

    }

}
