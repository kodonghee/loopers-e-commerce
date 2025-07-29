package com.loopers.domain.user;

public class UserCommand {

    public record Create (String userId, Gender gender, String birthDate, String email) {

    }

}
