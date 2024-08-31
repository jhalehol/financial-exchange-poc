package com.yellowpepper.challenge.financial.service;

import com.yellowpepper.challenge.financial.dto.UserDto;
import com.yellowpepper.challenge.financial.exception.InvalidArgumentsException;
import com.yellowpepper.challenge.financial.model.User;
import com.yellowpepper.challenge.financial.repository.UserRepository;
import com.yellowpepper.challenge.financial.model.Roles;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    private static final String ACCOUNT_REF = "0000001";
    private static final String USERNAME = "user-pepe";
    private static final String PASSWORD = "user-password";
    private static final String NAME = "Pipe Second";
    private static final Long USER_ID = 99L;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private UserService service;

    @Test
    public void givenUserNameWhenLoadByUserNameThenReturnUserDetails() {
        // Arrange
        final User user = getUser();
        when(userRepository.getFirstByUsernameEquals(USERNAME))
                .thenReturn(Optional.of(user));

        // Act
        final UserDetails userDetails = service.loadUserByUsername(USERNAME);

        // Assert
        verify(userRepository).getFirstByUsernameEquals(USERNAME);
        softly.assertThat(userDetails.getUsername()).isEqualTo(USERNAME);
    }

    @Test
    public void givenIdWhenLoadByUserIdThenReturnUserDetails() {
        // Arrange
        final User user = getUser();
        when(userRepository.getFirstByUserId(USER_ID))
                .thenReturn(Optional.of(user));

        // Act
        final User userDetails = service.loadUserByUserId(USER_ID);

        // Assert
        verify(userRepository).getFirstByUserId(USER_ID);
        softly.assertThat(userDetails.getUsername()).isEqualTo(USERNAME);
    }

    @Test
    public void givenUserDtoWhenCreateUserWithoutAccountThenShouldSaveUser()
            throws Exception {
        // Arrange
        final UserDto userDto = getUserDto();
        userDto.setAccounts(Collections.emptyList());

        // Act
        service.createUser(userDto);

        // Assert
        verify(userRepository).save(any(User.class));
        verify(accountService, never()).createUserAccount(any(User.class), anyString());
    }

    @Test
    public void givenUserDtoWithAccountWhenCreateUserThenShouldSaveUserAndAccount()
            throws Exception {
        // Arrange
        final UserDto userDto = getUserDto();

        // Act
        service.createUser(userDto);

        // Assert
        verify(userRepository).save(any(User.class));
        verify(accountService).createUserAccount(any(User.class), anyString());
    }

    @Test
    public void givenUserDtoWithExistingUserWhenCreateUserThenShouldFail()
            throws Exception {
        // Arrange
        final UserDto userDto = getUserDto();
        when(userRepository.existsUserByUsernameEquals(USERNAME)).thenReturn(true);

        expectedException.expect(InvalidArgumentsException.class);
        expectedException.expectMessage(String.format("Username %s is already used by another user", USERNAME));

        service.createUser(userDto);
    }

    @Test
    public void givenInvalidUserDtoWhenCreateUserAndAccountThenShouldFail()
            throws Exception {
        // Act & Assert
        expectedException.expect(InvalidArgumentsException.class);
        expectedException.expectMessage("User information not provided for creation");

        service.createUser(null);
    }

    private UserDto getUserDto() {
        return UserDto.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .name(NAME)
                .accounts(Collections.singletonList(ACCOUNT_REF))
                .build();
    }

    private User getUser() {
        final User user = mock(User.class);
        when(user.getRole()).thenReturn(Roles.ROLE_USER);
        when(user.getUsername()).thenReturn(USERNAME);
        when(userRepository.getFirstByUsernameEquals(USERNAME))
                .thenReturn(Optional.of(user));
        return user;
    }
}
