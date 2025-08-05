package com.eformsign.sample.dto;

import lombok.Data;

@Data
public class TemplateCreateRequest {
    private String accountId;
    private Long documentId;
    private String templateName;
}