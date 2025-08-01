package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.point.Point;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Entity
@Table(name = "member")
public class User extends BaseEntity {

    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String birthDate;
    private String email;

    protected User() {}

    public User(String userId, Gender gender, String birthDate, String email) {

        validateUserId(userId);
        validateEmail(email);
        validateBirthDate(birthDate);

        this.userId = userId;
        this.gender = gender;
        this.birthDate = birthDate;
        this.email = email;
    }

    public String getUserId() { return userId; }

    public Gender getGender() {
        return gender;
    }

    public String getBirthDate() { return birthDate; }

    public String getEmail() {
        return email;
    }

    // =============================
    // 🔒 Validation methods
    // =============================

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "ID는 빈 값이 될 수 없습니다.");
        }

        if (!userId.matches("^[a-zA-Z0-9]{1,10}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "ID는 영문 및 숫자 10자 이내여야 합니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일은 빈 값이 될 수 없습니다.");
        }

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일은 xx@yy.zz 형식에 맞아야 합니다.");
        }
    }

    private void validateBirthDate(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 빈 값이 될 수 없습니다.");
        }

        try {
            LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 yyyy-MM-dd 형식에 맞아야 합니다.");
        }
    }
}
