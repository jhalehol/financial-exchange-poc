package com.yellowpepper.challenge.financial.service.auth;

import com.yellowpepper.challenge.financial.config.AppConfiguration;
import com.yellowpepper.challenge.financial.exception.ConfigurationException;
import com.yellowpepper.challenge.financial.model.User;
import com.yellowpepper.challenge.financial.service.UserService;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JwtServiceTest {

    private static final Long USER_ID = 99L;
    private static Long EXPIRATION_MINS = 18396000L;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private UserService userService;

    @Mock
    private AppConfiguration appConfiguration;

    private JwtService jwtService;

    @Before
    public void setup() throws Exception {
        when(appConfiguration.getOauthPublicKeyPath()).thenReturn(OAuthTestUtils.getPublicKeyFilePath());
        when(appConfiguration.getOauthPrivateKeyPath()).thenReturn(OAuthTestUtils.getPrivateKeyFilePath());
        when(appConfiguration.getOauthTokenExpirationTimeMins()).thenReturn(EXPIRATION_MINS);
        jwtService = new JwtService(userService, appConfiguration);
    }

    @Test
    public void givenUserIdWhenBuildJwtThenReturnJwtToken() throws Exception {
        // Act
        final String jwt = jwtService.buildJwt(USER_ID);

        // Assert
        softly.assertThat(jwt).isNotEmpty();
    }

    @Test
    public void givenInvalidKeyWhenBuildJwtThenShouldFail() throws Exception {
        // Arrange
        when(appConfiguration.getOauthPublicKeyPath()).thenReturn(OAuthTestUtils.getPublicInvalidKeyPath());

        // Act & Assert
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Unable to initialize OAuth keys");

        new JwtService(userService, appConfiguration);
    }

    @Test
    public void givenJwtTokenWhenValidateJwtThenReturnUser() throws Exception {
        // Arrange
        final String jwt = OAuthTestUtils.getJwtContent();
        final User userDetails = mock(User.class);
        when(userService.loadUserByUserId(USER_ID)).thenReturn(userDetails);

        // Act
        final User userResult = jwtService.validateJwtToken(jwt);

        // Assert
        softly.assertThat(userResult).isEqualTo(userDetails);
    }
}
