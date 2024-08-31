package com.yellowpepper.challenge.financial.service;

import com.yellowpepper.challenge.financial.dto.UserAccountDto;
import com.yellowpepper.challenge.financial.exception.InvalidArgumentsException;
import com.yellowpepper.challenge.financial.exception.NotFoundException;
import com.yellowpepper.challenge.financial.exception.UnauthorizedException;
import com.yellowpepper.challenge.financial.model.Account;
import com.yellowpepper.challenge.financial.model.User;
import com.yellowpepper.challenge.financial.repository.AccountRepository;
import com.yellowpepper.challenge.financial.repository.UserRepository;
import com.yellowpepper.challenge.financial.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(final AccountRepository accountRepository,
            UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get details of the given account
     * @param accountRef Account reference
     * @return Account information
     * @throws NotFoundException
     * @throws InvalidArgumentsException
     */
    public Account getAccount(final String accountRef) throws NotFoundException,
            InvalidArgumentsException {
        if (StringUtils.isEmpty(accountRef)) {
            throw new InvalidArgumentsException("Account is empty or null");
        }

        return accountRepository.getFirstByAccountRefEquals(accountRef)
                .orElseThrow(() -> new NotFoundException(String.format("Account %s does not exist", accountRef)));
    }

    /**
     * Creates an account for the given user
     * @param user User owner of the account
     * @param accountRef Account reference
     * @throws InvalidArgumentsException
     */
    @Transactional
    public void createUserAccount(final User user, final String accountRef) throws InvalidArgumentsException {
        final boolean accountRefUsed = accountRepository.existsAccountByAccountRefEquals(accountRef);
        if (accountRefUsed) {
            throw new InvalidArgumentsException(String.format("Account reference %s is already in use",
                    accountRef));
        }

        final Account account = Account.builder()
                .user(user)
                .accountRef(accountRef)
                .build();
        log.debug("Creating user account {} for user {}", accountRef, user.getUsername());
        accountRepository.save(account);
    }

    /**
     * Return the list of accounts registered to the authenticated user
     * @return List of user accounts
     */
    public List<UserAccountDto> getAuthenticatedUserAccounts() throws UnauthorizedException {
        final UserDetailsImpl userDetails = getAuthenticatedUser();
        try {
            return getUserAccounts(userDetails.getUserId());
        } catch (NotFoundException e) {
            throw new UnauthorizedException(String.format("Unable to get accounts from authenticated user %s",
                    e.getMessage()), e);
        }
    }

    private UserDetailsImpl getAuthenticatedUser() {
        return(UserDetailsImpl) SecurityContextHolder.
                getContext().
                getAuthentication().
                getPrincipal();
    }

    private List<UserAccountDto> getUserAccounts(final Long userId) throws NotFoundException {
        final User user = userRepository.getFirstByUserId(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User %d not found", userId)));

        return accountRepository.getAccountByUser(user)
                .stream().map(account -> UserAccountDto.builder()
                        .accountId(account.getAccountId())
                        .accountRef(account.getAccountRef())
                        .build())
                .collect(Collectors.toList());
    }
}
