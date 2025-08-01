package com.eformsign.sample.controller;

import com.eformsign.sample.dto.TreeResponse;
import com.eformsign.sample.entity.Document;
import com.eformsign.sample.service.CategoryService;
import com.eformsign.sample.repository.DocumentRepository;
import com.eformsign.sample.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final CategoryService categoryService;
    private final DocumentRepository documentRepository;
    private final StorageRepository storageRepository;

    /**
     * 1. 카테고리 트리 조회
     */
    @GetMapping("/tree")
    public List<TreeResponse> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    /**
     * 2. 카테고리 ID 기준 문서 목록 조회
     */
    @GetMapping("/by-category/{categoryId}")
    public List<Document> getDocumentsByCategory(@PathVariable Long categoryId) {
        return documentRepository.findByCategoryId(categoryId);
    }

    /**
     * 3. 문서 상세 조회
     */
    @GetMapping("/{id}")
    public Document getDocumentById(@PathVariable Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문서 없음: id=" + id));
    }

    /**
     * 4. 문서 썸네일 이미지 반환
     */
    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<Resource> getThumbnail(@PathVariable Long id) throws MalformedURLException {
        String path = storageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("storage 없음"))
                .getPath();

        UrlResource resource = new UrlResource(Paths.get(path).toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    /**
     * 5. 문서 다운로드
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws MalformedURLException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문서 없음: id=" + id));

        String filePath = storageRepository.findById(document.getStorageId())
                .orElseThrow(() -> new IllegalArgumentException("Storage 없음: id=" + document.getStorageId()))
                .getPath();

        Resource resource = new UrlResource(Paths.get(filePath).toUri());

        String fileName = document.getTitle() + ".pdf"; // 필요에 따라 확장자 조정
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}