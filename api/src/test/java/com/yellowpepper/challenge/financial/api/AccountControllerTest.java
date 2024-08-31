package com.yellowpepper.challenge.financial.api;

import com.yellowpepper.challenge.financial.exception.InvalidArgumentsException;
import com.yellowpepper.challenge.financial.service.AccountService;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController controller;

    @Test
    public void givenRequestWhenGetAuthenticatedUserAccountsThenReturnOk() throws InvalidArgumentsException {
        // Arrange
        when(accountService.getAuthenticatedUserAccounts()).thenReturn(Collections.emptyList());

        // Act
        final ResponseEntity<?> response = controller.getUserAccounts();

        // Assert
        verify(accountService).getAuthenticatedUserAccounts();
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
