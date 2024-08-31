package com.yellowpepper.challenge.financial.security.filter;

import com.yellowpepper.challenge.financial.model.Roles;
import com.yellowpepper.challenge.financial.model.User;
import com.yellowpepper.challenge.financial.service.auth.JwtService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest(SecurityContextHolder.class)
@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
public class AuthFilterTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String JWT_TOKEN = "jwt_token";
    private static final String BEARER_TOKEN = "bearer " + JWT_TOKEN;
    private static final String NORMAL_PATH = "api/xyz";
    private static final String OAUTH2_PATH = "oauth2/another/path";
    private static final Long USER_ID = 1L;
    private static final String USER_NAME = "peter";

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthFilter authFilter;

    @Test
    public void givenValidRequestWithAuthorizationWhenFilterThenShouldAuthenticate()
            throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn(NORMAL_PATH);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_TOKEN);
        final User userDetails = User.builder()
                .userId(USER_ID)
                .name(USER_NAME)
                .role(Roles.ROLE_ADMIN)
                .build();

        when(jwtService.validateJwtToken(JWT_TOKEN)).thenReturn(userDetails);

        // Act
        authFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService).validateJwtToken(JWT_TOKEN);
    }

    @Test
    public void givenValidRequestForOAuth2WithAuthorizationWhenFilterThenShouldNotAuthenticate()
            throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn(OAUTH2_PATH);

        // Act
        authFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService, never()).validateJwtToken(anyString());
    }
}
