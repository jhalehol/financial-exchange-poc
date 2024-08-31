package com.yellowpepper.challenge.financial.dto;

import com.yellowpepper.challenge.financial.exception.InvalidArgumentsException;
import com.yellowpepper.challenge.financial.model.User;
import com.yellowpepper.challenge.financial.model.Roles;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserDtoTest {

    private static final String USERNAME = "peter";
    private static final String SURNAME = "parker";
    private static final String NAME = USERNAME;
    private static final String PASSWORD = "password";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void givenValidUserDtoWhenConvertToUserThenConvert() throws Exception {
        // Arrange
        final UserDto userDto = UserDto.builder()
                .name(NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .surname(SURNAME)
                .build();

        // Act
        final User user = userDto.toUser();

        // Assert
        softly.assertThat(user.getUsername()).isEqualTo(USERNAME);
        softly.assertThat(user.getPassword()).isNull();
        softly.assertThat(user.getRole()).isEqualTo(Roles.ROLE_USER);
        softly.assertThat(user.getName()).isEqualTo(NAME);
        softly.assertThat(user.getSurname()).isEqualTo(SURNAME);
    }

    @Test
    public void givenInvalidUserDtoWhenConvertToUserThenFail() throws Exception {
        // Arrange
        final UserDto userDto = UserDto.builder()
                .username(USERNAME)
                .build();

        // Act && Assert
        expectedException.expect(InvalidArgumentsException.class);
        expectedException.expectMessage("Password is required field");

        userDto.toUser();
    }
}
