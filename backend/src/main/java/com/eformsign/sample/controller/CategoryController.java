package com.eformsign.sample.controller;

import com.eformsign.sample.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 경로 조회 API
    @GetMapping("/path/{id}")
    public ResponseEntity<List<String>> getCategoryPath(@PathVariable("id") Long categoryId) {
        List<String> path = categoryService.getCategoryPath(categoryId);
        return ResponseEntity.ok(path);
    }
}