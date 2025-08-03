package com.eformsign.sample.controller;

import com.eformsign.sample.service.CategoryInitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryInitController {

    private final CategoryInitService categoryInitService;

    @PostMapping("/init")
    public ResponseEntity<String> initFromFolder(@RequestParam String path) {
        categoryInitService.initializeFromFolder(path);
        return ResponseEntity.ok("카테고리 및 문서 초기화 완료");
    }
}