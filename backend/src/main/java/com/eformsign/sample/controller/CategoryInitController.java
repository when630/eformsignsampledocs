package com.eformsign.sample.controller;

import com.eformsign.sample.service.CategoryInitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
public class CategoryInitController {

    private final CategoryInitService categoryInitService;

    @PostMapping("/initialize")
    public ResponseEntity<String> initialize(@RequestParam("file") MultipartFile file) {
        try {
            categoryInitService.initializeFromExcel(file.getInputStream());
            return ResponseEntity.ok("카테고리 및 문서 초기화 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("초기화 실패: " + e.getMessage());
        }
    }
}