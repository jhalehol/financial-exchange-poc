package com.yellowpepper.challenge.financial.repository;

import com.yellowpepper.challenge.financial.model.Account;
import com.yellowpepper.challenge.financial.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    Optional<Account> getFirstByAccountRefEquals(String accountRef);

    boolean existsAccountByAccountRefEquals(String accountRef);

    List<Account> getAccountByUser(User user);
}
