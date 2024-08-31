package com.yellowpepper.challenge.financial.service.auth;

import com.yellowpepper.challenge.financial.dto.AuthenticationDto;
import com.yellowpepper.challenge.financial.dto.TokenDto;
import com.yellowpepper.challenge.financial.exception.OperationException;
import com.yellowpepper.challenge.financial.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(JwtService jwtService, AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public TokenDto authenticateUser(final AuthenticationDto authenticationData) throws OperationException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationData.getUsername(),
                        authenticationData.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        final UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        final String jwt = jwtService.buildJwt(userDetails.getUserId());

        return TokenDto.builder()
                .token(jwt)
                .userName(userDetails.getName())
                .generatedAt(Instant.now().toEpochMilli())
                .build();
    }

    public UserDetailsImpl getAuthenticatedUser() {
        return(UserDetailsImpl) SecurityContextHolder.
                getContext().
                getAuthentication().
                getPrincipal();
    }
}
