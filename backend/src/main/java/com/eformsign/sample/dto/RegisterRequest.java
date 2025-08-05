package com.eformsign.sample.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String name;
    private String password;
    private String apiKey;
    private String secretKey;
    private String company_id;
}