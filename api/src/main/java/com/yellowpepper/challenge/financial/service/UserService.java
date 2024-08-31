package com.yellowpepper.challenge.financial.service;

import com.amazonaws.util.CollectionUtils;
import com.yellowpepper.challenge.financial.dto.UserDto;
import com.yellowpepper.challenge.financial.exception.InvalidArgumentsException;
import com.yellowpepper.challenge.financial.exception.UserNotFoundException;
import com.yellowpepper.challenge.financial.model.User;
import com.yellowpepper.challenge.financial.repository.UserRepository;
import com.yellowpepper.challenge.financial.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AccountService accountService;

    public UserService(final UserRepository userRepository, AccountService accountService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
    }

    /**
     * Loads an user using username
     * @param username Username assigned to the user
     * @return User object
     * @throws UserNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(final String username) throws UserNotFoundException {
        final User user = userRepository.getFirstByUsernameEquals(username)
                .orElseThrow(() -> new UserNotFoundException(String.format("User %s not found", username)));

        return new UserDetailsImpl(user);
    }

    /**
     * Loads an user by Id
     * @param userId User ID assigned to the user in the database
     * @return User object
     * @throws UserNotFoundException
     */
    public User loadUserByUserId(final Long userId) throws UserNotFoundException {
        return userRepository.getFirstByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("User %d not found", userId)));
    }

    @Transactional
    public void createUser(final UserDto userDto)
            throws InvalidArgumentsException {
        if (userDto == null) {
            throw new InvalidArgumentsException("User information not provided for creation");
        }

        final boolean usernameUsed = userRepository.existsUserByUsernameEquals(userDto.getUsername());
        if (usernameUsed) {
            throw new InvalidArgumentsException(String.format("Username %s is already used by another user",
                    userDto.getUsername()));
        }

        log.debug(String.format("Creating user %s", userDto.getUsername()));
        final User user = userDto.toUser();
        user.setPassword(userDto.getPassword());
        userRepository.save(user);

        if (!CollectionUtils.isNullOrEmpty(userDto.getAccounts())) {
            for (String account : userDto.getAccounts()) {
                accountService.createUserAccount(user, account);
            }
        }
    }
}
