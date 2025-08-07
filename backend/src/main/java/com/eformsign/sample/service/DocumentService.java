package com.eformsign.sample.service;

import com.eformsign.sample.entity.Account;
import com.eformsign.sample.entity.Storage;
import com.eformsign.sample.entity.Token;
import com.eformsign.sample.entity.Document;
import com.eformsign.sample.repository.AccountRepository;
import com.eformsign.sample.repository.DocumentRepository;
import com.eformsign.sample.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final AccountRepository accountRepository;
    private final TokenService tokenService;
    private final StorageRepository storageRepository;
    private final DocumentRepository documentRepository;

    public Map<String, Object> getBase64WithToken(Long documentId, Long accountId) throws IOException {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("계정이 존재하지 않습니다."));

        Token token = tokenService.issueToken(account);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서가 존재하지 않습니다."));

        Storage storage = storageRepository.findById(document.getStorageId())
                .orElseThrow(() -> new IllegalArgumentException("파일이 존재하지 않습니다."));

        Path filePath = Paths.get(storage.getPath());
        byte[] fileBytes = Files.readAllBytes(filePath);
        String base64 = Base64.getEncoder().encodeToString(fileBytes);

        // 4. 응답 구성
        return Map.of(
                "base64", base64,
                "token", Map.of(
                        "access_token", token.getAccessToken(),
                        "refresh_token", token.getRefreshToken()
                ),
                "account", Map.of(
                        "id", account.getId(),
                        "name", account.getName(),
                        "email", account.getEmail(),
                        "company_id", account.getCompanyId()  // 이거 반드시 있어야 함
                ),
                "document", Map.of(
                        "id", document.getId(),
                        "title", document.getTitle(),
                        "storageId", storage.getId()
                )
        );
    }
}
