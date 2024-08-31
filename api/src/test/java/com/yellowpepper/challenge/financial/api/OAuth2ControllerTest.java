package com.yellowpepper.challenge.financial.api;

import com.yellowpepper.challenge.financial.dto.AuthenticationDto;
import com.yellowpepper.challenge.financial.dto.TokenDto;
import com.yellowpepper.challenge.financial.exception.OperationException;
import com.yellowpepper.challenge.financial.service.auth.AuthService;
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

import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OAuth2ControllerTest {

   private static final String TOKEN = "token-xyz";

   @Rule
   public ExpectedException expectedException = ExpectedException.none();

   @Rule
   public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

   @Mock
   private AuthService authService;

   @InjectMocks
   private OAuth2Controller oAuth2Controller;

   @Test
   public void givenValidAuthenticationWhenGetTokenThenReturnToken() throws Exception {
      // Arrange
      final AuthenticationDto authenticationDto = new AuthenticationDto();
      final TokenDto tokenDto = TokenDto.builder()
              .token(TOKEN)
              .generatedAt(Instant.now().toEpochMilli())
              .build();
      when(authService.authenticateUser(authenticationDto)).thenReturn(tokenDto);

      // Act
      final ResponseEntity<?> response = oAuth2Controller.getToken(new AuthenticationDto());

      // Assert
      softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      verify(authService).authenticateUser(authenticationDto);
   }

   @Test
   public void givenValidAuthenticationWhenGetTokenFailsThenReturnUnauthorized() throws Exception {
      // Arrange
      final AuthenticationDto authenticationDto = new AuthenticationDto();
      when(authService.authenticateUser(authenticationDto)).thenThrow(new OperationException("Error"));

      // Act
      final ResponseEntity<?> response = oAuth2Controller.getToken(new AuthenticationDto());

      // Assert
      softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      verify(authService).authenticateUser(authenticationDto);
   }
}
