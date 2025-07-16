package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Entity
@Table(name = "member")
public class User extends BaseEntity {

    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;
    private String gender;
    private String birthDate;
    private String email;

    protected User() {}

    public User(String userId, String gender, String birthDate, String email) {

        validateUserId(userId);
        validateEmail(email);
        validateBirthDate(birthDate);
        validateGender(gender);

        this.userId = userId;
        this.gender = gender;
        this.birthDate = birthDate;
        this.email = email;
    }

    public String getUserId() { return userId; }

    public String getGender() {
        return gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getEmail() {
        return email;
    }

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

    private void validateGender(String gender) {
        if (gender == null || gender.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 빈 값이 될 수 없습니다.");
        }

        if (!"F".equals(gender) && !"M".equals(gender)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 'F' 또는 'M'이어야 합니다.");
        }
    }

}
