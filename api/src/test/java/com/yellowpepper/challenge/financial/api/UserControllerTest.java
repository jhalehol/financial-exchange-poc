package com.yellowpepper.challenge.financial.api;

import com.yellowpepper.challenge.financial.dto.UserDto;
import com.yellowpepper.challenge.financial.service.UserService;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    @Test
    public void givenRequestWhenAddUserAccountThenShouldCallService() throws Exception {
        // Arrange
        final UserDto userDto = new UserDto();

        // Act
        final ResponseEntity<?> response = controller.createUser(userDto);

        // Assert
        verify(userService).createUser(userDto);
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
