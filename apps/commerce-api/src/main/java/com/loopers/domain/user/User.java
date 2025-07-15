package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
}
