package com.eformsign.sample.repository;

import com.eformsign.sample.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);

    String id(Long id);

    String email(String email);
}