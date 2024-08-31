package com.yellowpepper.challenge.financial.service.auth;

import com.yellowpepper.challenge.financial.dto.AuthenticationDto;
import com.yellowpepper.challenge.financial.dto.TokenDto;
import com.yellowpepper.challenge.financial.model.Roles;
import com.yellowpepper.challenge.financial.model.User;
import com.yellowpepper.challenge.financial.security.UserDetailsImpl;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@PrepareForTest(SecurityContextHolder.class)
@PowerMockIgnore("javax.security.*")
@RunWith(PowerMockRunner.class)
public class AuthServiceTest {

    private static final Long USER_ID = 99L;
    private static final String JWT_TOKEN = "jwt-token";

    @Rule
    private JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService service;

    @Before
    public void setup() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new UserDetailsImpl(User.builder()
                .role(Roles.ROLE_USER)
                .userId(USER_ID)
                .build()));
    }

    @Test
    public void givenAuthenticationDtoWhenAuthenticateUserThenShouldCallTokenBuilder() throws Exception {
        // Arrange
        final AuthenticationDto authenticationDto = new AuthenticationDto();
        when(jwtService.buildJwt(anyLong())).thenReturn(JWT_TOKEN);

        // Act
        final TokenDto result = service.authenticateUser(authenticationDto);

        // Assert
        softly.assertThat(result.getToken()).isEqualTo(JWT_TOKEN);
    }
}
