package com.yellowpepper.challenge.financial.service;

import com.yellowpepper.challenge.financial.dto.UserAccountDto;
import com.yellowpepper.challenge.financial.exception.InvalidArgumentsException;
import com.yellowpepper.challenge.financial.exception.NotFoundException;
import com.yellowpepper.challenge.financial.model.Account;
import com.yellowpepper.challenge.financial.model.Roles;
import com.yellowpepper.challenge.financial.model.User;
import com.yellowpepper.challenge.financial.repository.AccountRepository;
import com.yellowpepper.challenge.financial.repository.UserRepository;
import com.yellowpepper.challenge.financial.security.UserDetailsImpl;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest(SecurityContextHolder.class)
@PowerMockIgnore("javax.security.*")
@RunWith(PowerMockRunner.class)
public class AccountServiceTest {

    private static Long USER_ID = 99L;
    private static Long ACCOUNT_ID = 999L;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private static final String ACCOUNT_REF = "000000338813";

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AccountService service;

    @Before
    public void setup() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void givenAccountRefWhenGetAccountThenReturnAccount() throws Exception {
        // Arrange
        final Account account = mock(Account.class);
        when(accountRepository.getFirstByAccountRefEquals(ACCOUNT_REF))
                .thenReturn(Optional.of(account));

        // Act
        final Account accountResult = service.getAccount(ACCOUNT_REF);

        // Assert
        softly.assertThat(accountResult).isEqualTo(account);
    }

    @Test
    public void givenInvalidAccountRefWhenGetAccountThenShouldFail() throws Exception {
        // Act && Assert
        expectedException.expect(InvalidArgumentsException.class);
        expectedException.expectMessage("Account is empty or null");

        service.getAccount("");
    }

    @Test
    public void givenAccountRefDoesNotExistWhenGetAccountThenShouldFail() throws Exception {
        // Arrange
        when(accountRepository.getFirstByAccountRefEquals(ACCOUNT_REF))
                .thenReturn(Optional.empty());

        // Act && Assert
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(String.format("Account %s does not exist", ACCOUNT_REF));

        service.getAccount(ACCOUNT_REF);
    }

    @Test
    public void givenUserAndAccountWhenCreateUserAccountThenSaveAccount() throws Exception {
        // Arrange
        final User user = mock(User.class);
        when(accountRepository.existsAccountByAccountRefEquals(ACCOUNT_REF))
                .thenReturn(true);

        // Act && Assert
        expectedException.expect(InvalidArgumentsException.class);
        expectedException.expectMessage(String.format("Account reference %s is already in use",
                ACCOUNT_REF));

        service.createUserAccount(user, ACCOUNT_REF);
    }

    @Test
    public void givenUserAndExistingAccountWhenCreateUserAccountThenShouldFail() throws Exception {
        // Arrange
        final User user = mock(User.class);
        when(user.getUserId()).thenReturn(USER_ID);

        // Act
        service.createUserAccount(user, ACCOUNT_REF);

        // Assert
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    public void givenAuthenticatedUserWhenGetUserAccountsThenReturnListAccounts() throws Exception {
        // Arrange
        final User user = User.builder()
                .userId(USER_ID)
                .role(Roles.ROLE_USER)
                .build();
        final UserDetailsImpl userDetails = new UserDetailsImpl(user);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.getFirstByUserId(USER_ID)).thenReturn(Optional.of(user));
        final Account account = Account.builder()
                .accountRef(ACCOUNT_REF)
                .accountId(ACCOUNT_ID)
                .build();
        final List<Account> accounts = Collections.singletonList(account);
        when(accountRepository.getAccountByUser(user)).thenReturn(accounts);

        // Act
        final List<UserAccountDto> result = service.getAuthenticatedUserAccounts();

        // Assert
        softly.assertThat(result.size()).isEqualTo(accounts.size());
        softly.assertThat(result.get(0).getAccountRef()).isEqualTo(ACCOUNT_REF);
    }
}
