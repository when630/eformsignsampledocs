package com.eformsign.sample.service;

import com.eformsign.sample.entity.Account;
import com.eformsign.sample.entity.Storage;
import com.eformsign.sample.entity.Token;
import com.eformsign.sample.entity.Document;
import com.eformsign.sample.repository.AccountRepository;
import com.eformsign.sample.repository.DocumentRepository;
import com.eformsign.sample.repository.StorageRepository;
import com.eformsign.sample.util.DocToPdfUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
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
        File file = filePath.toFile();

        File fileToSend;

        // 4. 확장자가 doc 또는 docx라면 PDF 변환
        String lowerName = file.getName().toLowerCase();
        if (lowerName.endsWith(".doc") || lowerName.endsWith(".docx")) {
            try {
                fileToSend = DocToPdfUtil.getOrConvertPdfFromDoc(file);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 인터럽트 상태 복구
                throw new RuntimeException("PDF 변환 중 인터럽트 발생", e);
            }
        } else {
            fileToSend = file; // 이미 PDF면 그대로 사용
        }

        // 5. base64 변환
        byte[] fileBytes = Files.readAllBytes(fileToSend.toPath());
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
