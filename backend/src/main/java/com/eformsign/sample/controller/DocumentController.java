package com.eformsign.sample.controller;

import com.eformsign.sample.dto.TreeResponse;
import com.eformsign.sample.entity.Account;
import com.eformsign.sample.entity.Document;
import com.eformsign.sample.entity.Storage;
import com.eformsign.sample.repository.DocumentRepository;
import com.eformsign.sample.repository.StorageRepository;
import com.eformsign.sample.service.CategoryService;
import com.eformsign.sample.service.DownloadLogService;
import com.eformsign.sample.util.ThumbnailUtil;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
@Slf4j
public class DocumentController {

    private final CategoryService categoryService;
    private final DocumentRepository documentRepository;
    private final StorageRepository storageRepository;
    private final DownloadLogService downloadLogService;

    // 1. 카테고리 트리 조회
    @GetMapping("/tree")
    public List<TreeResponse> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    // 2. 카테고리 기준 문서 목록 조회
    @GetMapping("/by-category/{categoryId}")
    public List<Document> getDocumentsByCategory(@PathVariable Long categoryId) {
        List<Long> depth3Ids = categoryService.getDepth3CategoryIdsUnder(categoryId);
        return documentRepository.findByCategoryIdIn(depth3Ids);
    }

    // 3. 문서 상세 조회
    @GetMapping("/{id}")
    public Document getDocumentById(@PathVariable Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문서 없음: id=" + id));
    }

    // 4. 문서 썸네일 반환 (PDF, DOCX 지원)
    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<Resource> getThumbnail(@PathVariable Long id) throws Exception {
        log.info("getThumbnail 호출됨, id={}", id);
        Storage storage = storageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("storage 없음: id=" + id));

        File file = new File(storage.getPath());
        if (!file.exists()) {
            throw new IllegalArgumentException("파일 없음: " + storage.getPath());
        }

        String extension = getExtension(file.getName()).toLowerCase();
        File thumbnailImage;

        switch (extension) {
            case "pdf":
                thumbnailImage = ThumbnailUtil.generatePdfThumbnail(file);
                break;
            case "docx":
                thumbnailImage = ThumbnailUtil.generateDocxThumbnail(file);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 파일 형식: " + extension);
        }

        log.info("원본 파일 경로: {}", file.getAbsolutePath());
        log.info("생성된 썸네일 경로: {}", thumbnailImage.getAbsolutePath());
        log.info("파일 존재 여부: {}", thumbnailImage.exists());
        Resource resource = new UrlResource(thumbnailImage.toURI());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + thumbnailImage.getName() + "\"")
                .body(resource);
    }

    // 5. 문서 다운로드
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, @RequestParam Long accountId, HttpServletRequest request) throws MalformedURLException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문서 없음: id=" + id));

        String filePath = storageRepository.findById(document.getStorageId())
                .orElseThrow(() -> new IllegalArgumentException("Storage 없음: id=" + document.getStorageId()))
                .getPath();

        Resource resource = new UrlResource(Paths.get(filePath).toUri());
        String fileName = document.getTitle() + ".docx";

        // 다운로드 로그 저장
        downloadLogService.logDownload(document.getId().toString(), accountId, request);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }
}