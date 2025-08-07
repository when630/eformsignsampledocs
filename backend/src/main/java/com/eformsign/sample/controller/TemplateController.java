package com.eformsign.sample.controller;

import com.eformsign.sample.dto.TemplateCreateRequest;
import com.eformsign.sample.dto.TemplateRequest;
import com.eformsign.sample.entity.Account;
import com.eformsign.sample.entity.Document;
import com.eformsign.sample.entity.Storage;
import com.eformsign.sample.entity.Token;
import com.eformsign.sample.repository.AccountRepository;
import com.eformsign.sample.repository.DocumentRepository;
import com.eformsign.sample.repository.StorageRepository;
import com.eformsign.sample.repository.TokenRepository;
import com.eformsign.sample.service.EformsignService;
import com.eformsign.sample.util.DocxToPdfUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/eformsign")
public class TemplateController {

    private final AccountRepository accountRepository;
    private final TokenRepository tokenRepository;
    private final DocumentRepository documentRepository;
    private final StorageRepository storageRepository;
    private final EformsignService eformsignService;

    @PostMapping("/template-create")
    public Map<String, Object> createTemplate(@RequestBody TemplateCreateRequest request) throws IOException, InterruptedException {
        // 1. 계정 조회
        String email = request.getAccountId();

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found for email: " + email));

        // 2. 토큰 조회
        Token token = tokenRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new RuntimeException("Token not found"));

        // 3. 문서 조회
        Long documentId = request.getDocumentId();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // 4. 파일 경로 조회
        Storage storage = storageRepository.findById(document.getStorageId())
                .orElseThrow(() -> new RuntimeException("Storage not found"));
        String filePath = storage.getPath();

        // 5. docx → pdf 변환 처리
        File file;
        if (filePath.toLowerCase().endsWith(".docx")) {
            file = DocxToPdfUtil.getOrConvertPdfFromDocx(new File(filePath));
        } else {
            file = new File(filePath);
        }

        // 6. 파일 → base64 인코딩
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String base64File = Base64.getEncoder().encodeToString(fileBytes);

        // 7. TemplateRequest 구성
        TemplateRequest templateRequest = new TemplateRequest();
        templateRequest.setCompanyId(account.getCompanyId());
        templateRequest.setCountryCode("ko");
        templateRequest.setUserId(account.getEmail());
        templateRequest.setAccessToken(token.getAccessToken());
        templateRequest.setRefreshToken(token.getRefreshToken());
        templateRequest.setTemplateName(request.getTemplateName());
        templateRequest.setBase64File(base64File);

        // 8. 템플릿 생성 요청
        return eformsignService.createTemplateOption(templateRequest);
    }

    @PostMapping("/template-option")
    public Map<String, Object> getTemplateOption(@RequestBody TemplateCreateRequest request) throws IOException, InterruptedException {
        // 1. 계정 조회
        String email = request.getAccountId();

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found for email: " + email));

        // 2. 토큰 조회
        Token token = tokenRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new RuntimeException("Token not found"));

        // 3. 문서 조회
        Long documentId = request.getDocumentId();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // 4. 파일 경로 조회
        Storage storage = storageRepository.findById(document.getStorageId())
                .orElseThrow(() -> new RuntimeException("Storage not found"));
        String filePath = storage.getPath();

        // 5. docx → pdf 변환 처리
        File file;
        if (filePath.toLowerCase().endsWith(".docx")) {
            file = DocxToPdfUtil.getOrConvertPdfFromDocx(new File(filePath));
        } else {
            file = new File(filePath);
        }

        // 6. 파일 → base64 인코딩
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String base64File = Base64.getEncoder().encodeToString(fileBytes);

        // 7. TemplateRequest 구성
        TemplateRequest templateRequest = new TemplateRequest();
        templateRequest.setCompanyId(account.getCompanyId());
        templateRequest.setCountryCode("ko");
        templateRequest.setUserId(account.getEmail());
        templateRequest.setAccessToken(token.getAccessToken());
        templateRequest.setRefreshToken(token.getRefreshToken());
        templateRequest.setTemplateName(request.getTemplateName());
        templateRequest.setBase64File(base64File);

        // 8. 템플릿 옵션 생성
        Map<String, Object> option = eformsignService.createTemplateOption(templateRequest);

        // 9. 프론트에 전달
        return Map.of(
                "api_key", account.getApiKey(),
                "access_token", token.getAccessToken(),
                "option", option
        );
    }
}