package com.eformsign.sample.controller;

import com.eformsign.sample.dto.LoginRequest;
import com.eformsign.sample.entity.Account;
import com.eformsign.sample.repository.AccountRepository;
import com.eformsign.sample.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import com.eformsign.sample.dto.RegisterRequest;


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

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refresh_token");

        if (!StringUtils.hasText(refreshToken) || !jwtProvider.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(401).body("Invalid or expired refresh token");
        }

        String email = jwtProvider.getEmailFromToken(refreshToken);
        String newAccessToken = jwtProvider.createAccessToken(email);

        Map<String, Object> response = new HashMap<>();
        response.put("access_token", newAccessToken);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        System.out.println("현재 인증 객체: " + authentication);

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("인증된 사용자 없음");
        }

        Object principal = authentication.getPrincipal();
        System.out.println(">> Principal: " + principal);

        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            return ResponseEntity.ok(Map.of("email", userDetails.getUsername()));
        } else {
            return ResponseEntity.status(401).body("올바르지 않은 사용자 정보");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        System.out.println("=== 회원가입 시도 ===");
        System.out.println("이메일: " + request.getEmail());

        boolean exists = accountRepository.findByEmail(request.getEmail()).isPresent();
        if (exists) {
            return ResponseEntity.status(409).body("이미 존재하는 이메일입니다.");
        }

        Account newAccount = new Account();
        newAccount.setEmail(request.getEmail());
        newAccount.setName(request.getName());
        newAccount.setPassword(request.getPassword()); // 추후 암호화 예정

        accountRepository.save(newAccount);

        return ResponseEntity.status(201).body("회원가입 성공");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        System.out.println("로그아웃 요청됨");
        return ResponseEntity.ok(Map.of("message", "로그아웃 완료. 클라이언트에서 토큰을 삭제해주세요."));
    }
}