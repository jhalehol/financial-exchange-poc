package com.yellowpepper.challenge.financial.api;

import com.yellowpepper.challenge.financial.dto.UserAccountDto;
import com.yellowpepper.challenge.financial.exception.UnauthorizedException;
import com.yellowpepper.challenge.financial.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Gets the user accounts of the current authenticated user given
     * the security token
     * @return List of accounts
     */
    @GetMapping(value = "list/authenticated", produces = "application/json")
    public ResponseEntity<?> getUserAccounts() {
        final List<UserAccountDto> userAccounts;
        try {
            userAccounts = accountService
                    .getAuthenticatedUserAccounts();
            return ResponseEntity.ok(userAccounts);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
