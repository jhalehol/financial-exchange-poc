package com.yellowpepper.challenge.financial.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "fin_users")
public class User {

    @Transient
    private PasswordEncoder passwordEncoder;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Roles role;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (passwordEncoder == null) {
            passwordEncoder = new BCryptPasswordEncoder();
        }

        this.password = passwordEncoder.encode(password);
    }

    public String getCompleteName() {
        return String
                .format("%s %s", name, StringUtils.isEmpty(surname) ? "" : surname)
                .trim();
    }
}
