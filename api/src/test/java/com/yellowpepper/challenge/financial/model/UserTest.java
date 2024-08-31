package com.yellowpepper.challenge.financial.model;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@RunWith(MockitoJUnitRunner.class)
public class UserTest {

    private static final String PASSWORD = "mypassword";

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private PasswordEncoder passwordEncoder;

    @Before
    public void setup() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    public void givenPasswordWhenSetPasswordShouldEncode() {
        // Arrange
        final User user = new User();
        user.setPassword(PASSWORD);

        // Act
        final String encodedPassword = user.getPassword();

        // Assert
        final boolean matches = passwordEncoder.matches(PASSWORD, encodedPassword);
        softly.assertThat(matches).isTrue();
    }
}
