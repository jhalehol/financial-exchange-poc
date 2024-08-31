package com.yellowpepper.challenge.financial.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class AuthEntryPointTest {

    private static final String PATH = "/api/path";
    private static final String ERROR = "authentication error";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authenticationException;

    private AuthEntryPoint authEntryPoint;

    @Before
    public void setup() {
        authEntryPoint = new AuthEntryPoint();
    }

    @Test
    public void givenAuthenticationExceptionWhenCommenceWriteInResponse() throws Exception {
        // Arrange
        when(request.getServletPath()).thenReturn(PATH);
        when(authenticationException.getMessage()).thenReturn(ERROR);
        final ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);

        // Act
        authEntryPoint.commence(request, response, authenticationException);

        // Assert
        verify(authenticationException).getMessage();
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(APPLICATION_JSON_VALUE);
        verify(response).getOutputStream();
    }
}
