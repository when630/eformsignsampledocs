package com.eformsign.sample.repository;

import com.eformsign.sample.entity.Account;
import com.eformsign.sample.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByAccountId(Long accountId);
    Optional<Token> findByAccount(Account account);
    Optional<Token> findByRefreshToken(String refreshToken);

}