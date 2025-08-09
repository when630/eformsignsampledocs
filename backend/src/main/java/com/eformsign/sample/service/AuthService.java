package com.eformsign.sample.service;

import com.eformsign.sample.dto.LoginRequest;
import com.eformsign.sample.dto.RegisterRequest;
import com.eformsign.sample.entity.Account;
import com.eformsign.sample.entity.Token;
import com.eformsign.sample.repository.AccountRepository;
import com.eformsign.sample.repository.TokenRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AccountRepository accountRepository;
    private final TokenRepository tokenRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AccountRepository accountRepository, TokenRepository tokenRepository, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.tokenRepository = tokenRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> register(RegisterRequest request) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body("이미 존재하는 이메일입니다.");
        }

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setName(request.getName());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setApiKey(request.getApiKey());
        account.setSecretKey(request.getSecretKey());
        account.setCompanyId(request.getCompany_id());
        accountRepository.save(account);

        return ResponseEntity.ok("회원가입 완료");
    }

    public ResponseEntity<?> login(LoginRequest request) {
        return tokenService.issueToken(request);
    }
}