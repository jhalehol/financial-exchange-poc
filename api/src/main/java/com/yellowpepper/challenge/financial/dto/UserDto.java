package com.yellowpepper.challenge.financial.dto;

import com.yellowpepper.challenge.financial.exception.InvalidArgumentsException;
import com.yellowpepper.challenge.financial.model.User;
import com.yellowpepper.challenge.financial.model.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String username;
    private String password;
    private String name;
    private String surname;
    private List<String> accounts;

    public User toUser() throws InvalidArgumentsException {
        validateBasicFields();

        return User.builder()
                .username(username)
                .name(name)
                .surname(surname)
                .role(Roles.ROLE_USER)
                .build();
    }

    private void validateBasicFields() throws InvalidArgumentsException {
        if (StringUtils.isEmpty(username)) {
            throw new InvalidArgumentsException("Username is required field");
        }

        if (StringUtils.isEmpty(password)) {
            throw new InvalidArgumentsException("Password is required field");
        }

        if (StringUtils.isEmpty(name)) {
            throw new InvalidArgumentsException("Name is required field");
        }
    }
}
