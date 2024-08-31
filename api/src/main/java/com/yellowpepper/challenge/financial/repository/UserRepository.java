package com.yellowpepper.challenge.financial.repository;

import com.yellowpepper.challenge.financial.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> getFirstByUsernameEquals(String username);

    Optional<User> getFirstByUserId(Long userId);

    boolean existsUserByUsernameEquals(String username);
}
