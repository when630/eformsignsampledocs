package com.eformsign.sample.controller;

import com.eformsign.sample.dto.LoginRequest;
import com.eformsign.sample.dto.RegisterRequest;
import com.eformsign.sample.entity.Account;
import com.eformsign.sample.entity.Token;
import com.eformsign.sample.repository.AccountRepository;
import com.eformsign.sample.repository.TokenRepository;
import com.eformsign.sample.service.AuthService;
import com.eformsign.sample.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        return tokenService.refreshToken(body.get("refresh_token"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String bearerToken) {
        return tokenService.getMe(bearerToken);
    }
}