package com.eformsign.sample.repository;

import com.eformsign.sample.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
}