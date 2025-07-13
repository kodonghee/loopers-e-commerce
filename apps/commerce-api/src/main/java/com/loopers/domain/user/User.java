package com.loopers.domain.user;

import com.loopers.domain.example.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user")
public class User extends BaseEntity {

    @Id
    private String id;
    private String email;
    private String birthday;

    protected User() {}

    public User(String id, String email, String birthday) {
        this.id = id;
        this.email = email;
        this.birthday = birthday;
    }
}
