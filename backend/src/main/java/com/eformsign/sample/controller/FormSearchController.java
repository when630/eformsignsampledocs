// src/main/java/.../controller/FormSearchController.java
package com.eformsign.sample.controller;

import com.eformsign.sample.entity.Document;
import com.eformsign.sample.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormSearchController {

    private final DocumentRepository documentRepository;

    @GetMapping("/search-title-exact")
    public Page<Document> searchTitleExact(
            @RequestParam String q,
            @RequestParam(defaultValue = "WORD") String mode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        String query = q == null ? "" : q.trim();
        if (query.isEmpty()) return Page.empty(pageable);

        if ("EQUAL".equalsIgnoreCase(mode)) {
            return documentRepository.findByTitleEqualsIgnoreCase(query, pageable);
        } else {
            try {
                return documentRepository.findByTitleWord(query, pageable);
            } catch (Exception e) {
                // ★ 여기!
                return documentRepository.findByTitleContainingIgnoreCase(query, pageable);
            }
        }
    }
}