package com.eformsign.sample.controller;

import com.eformsign.sample.dto.LoginRequest;
import com.eformsign.sample.entity.Account;
import com.eformsign.sample.repository.AccountRepository;
import com.eformsign.sample.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("=== 로그인 시도 ===");
        System.out.println("이메일: " + loginRequest.getEmail());
        System.out.println("비밀번호: " + loginRequest.getPassword());

        Account account = accountRepository.findByEmail(loginRequest.getEmail())
                .orElse(null);

        if (account == null) {
            System.out.println(">> 계정 없음");
            return ResponseEntity.status(401).body("존재하지 않는 계정입니다.");
        }

        if (!account.getPassword().equals(loginRequest.getPassword())) {
            System.out.println(">> 비밀번호 불일치");
            return ResponseEntity.status(401).body("비밀번호가 틀렸습니다.");
        }

        System.out.println(">> 로그인 성공");

        String accessToken = jwtProvider.createAccessToken(account.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(account.getEmail());

        System.out.println(">> accessToken = " + accessToken);

        Map<String, Object> response = new HashMap<>();
        response.put("access_token", accessToken);
        response.put("refresh_token", refreshToken);
        response.put("email", account.getEmail());
        response.put("name", account.getName());

        return ResponseEntity.ok(response);
    }
}