package com.loopers.domain.user;

public class UserCommand {

    public record Create (String userId, String gender, String birthDate, String email) {

    }

}
