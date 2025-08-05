package com.eformsign.sample.service;

import com.eformsign.sample.dto.LoginRequest;
import com.eformsign.sample.entity.Account;
import com.eformsign.sample.entity.Token;
import com.eformsign.sample.repository.AccountRepository;
import com.eformsign.sample.repository.TokenRepository;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenService {

    private final AccountRepository accountRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();

    public TokenService(AccountRepository accountRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> issueToken(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일 없음"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            return ResponseEntity.status(401).body("비밀번호 오류");
        }

        String base64Key = Base64.getEncoder().encodeToString(account.getApiKey().getBytes());
        long executionTime = System.currentTimeMillis();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + base64Key);
        headers.set("eformsign_signature", "Bearer " + account.getSecretKey());

        Map<String, Object> body = new HashMap<>();
        body.put("execution_time", executionTime);
        body.put("member_id", account.getEmail());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity("https://api.eformsign.com/v2.0/api_auth/access_token", requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> oauth = (Map<String, Object>) response.getBody().get("oauth_token");

            // ✅ 토큰 존재 여부 확인 후 update or insert
            Token token = tokenRepository.findByAccount(account)
                    .orElse(new Token()); // 없으면 새 객체

            token.setAccount(account);
            token.setAccessToken((String) oauth.get("access_token"));
            token.setRefreshToken((String) oauth.get("refresh_token"));
            token.setExpiresAt(LocalDateTime.now().plusHours(1));
            tokenRepository.save(token);

            Map<String, Object> result = new HashMap<>();
            result.put("token", oauth);
            result.put("account", Map.of(
                    "id", account.getId(),
                    "name", account.getName(),
                    "email", account.getEmail()
            ));

            return ResponseEntity.ok(result);
        }

        return ResponseEntity.status(500).body("이폼사인 토큰 발급 실패");
    }

    public ResponseEntity<?> refreshToken(String refreshToken) {
        return ResponseEntity.status(501).body("Not implemented");
    }

    public ResponseEntity<?> getMe(String bearerToken) {
        return ResponseEntity.ok("토큰 확인 완료: " + bearerToken);
    }
}