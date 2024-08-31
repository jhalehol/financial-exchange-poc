package com.yellowpepper.challenge.financial.api;

import com.yellowpepper.challenge.financial.dto.AuthenticationDto;
import com.yellowpepper.challenge.financial.dto.TokenDto;
import com.yellowpepper.challenge.financial.exception.OperationException;
import com.yellowpepper.challenge.financial.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2")
public class OAuth2Controller {

    private final AuthService authService;

    public OAuth2Controller(AuthService authService) {
        this.authService = authService;
    }

    @CrossOrigin
    @PostMapping("/token")
    public ResponseEntity<?> getToken(@RequestBody AuthenticationDto authentication) {
        try {
            final TokenDto jwt = authService.authenticateUser(authentication);

            return ResponseEntity.ok(jwt);
        } catch (OperationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
