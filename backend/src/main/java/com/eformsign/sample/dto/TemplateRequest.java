package com.eformsign.sample.dto;

import lombok.Data;
import java.util.List;

@Data
public class TemplateRequest {
    private String companyId;
    private String countryCode;
    private String userId;
    private String accessToken;
    private String refreshToken;
    private String templateName;
    private String filePath; // 로컬 PDF 파일 경로
    private List<String> recipients; // 이메일 or user id
}