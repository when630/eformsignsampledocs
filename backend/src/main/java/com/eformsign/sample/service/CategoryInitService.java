package com.eformsign.sample.service;

import com.eformsign.sample.entity.Category;
import com.eformsign.sample.entity.CategoryClosure;
import com.eformsign.sample.entity.Document;
import com.eformsign.sample.entity.Storage;
import com.eformsign.sample.repository.CategoryClosureRepository;
import com.eformsign.sample.repository.CategoryRepository;
import com.eformsign.sample.repository.DocumentRepository;
import com.eformsign.sample.repository.StorageRepository;
import com.eformsign.sample.util.ExcelReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryInitService {

    private final ExcelReader excelReader;
    private final CategoryRepository categoryRepository;
    private final CategoryClosureRepository closureRepository;
    private final DocumentRepository documentRepository;
    private final StorageRepository storageRepository;

    // 카테고리 이름 → ID 캐시
    private final Map<String, Long> categoryCache = new HashMap<>();

    // 조상-자손 관계 캐시 (중복 insert 방지)
    private final Set<String> closureCache = new HashSet<>();

    /**
     * 엑셀 파일 스트림을 읽어 카테고리, 문서, 계층 관계 초기화
     */
    @Transactional
    public void initializeFromExcel(InputStream excelStream) throws Exception {
        List<List<String>> rows = excelReader.readExcel(excelStream);

        String lastDepth1 = null;
        String lastDepth2 = null;

        for (List<String> row : rows) {
            if (row.size() < 3) continue;

            String cell0 = row.get(0) != null ? row.get(0).trim() : "";
            String cell1 = row.get(1) != null ? row.get(1).trim() : "";
            String cell2 = row.get(2) != null ? row.get(2).trim() : "";

            if (!cell0.isBlank()) lastDepth1 = cell0;
            if (!cell1.isBlank()) lastDepth2 = cell1;

            if (lastDepth1 == null || lastDepth2 == null || cell2.isBlank()) {
                continue; // depth1, 2가 없는 상태에서 depth3만 있으면 무시
            }

            String depth1 = lastDepth1;
            String depth2 = lastDepth2;
            String depth3 = cell2;

            Long d1Id = ensureCategory(depth1);
            Long d2Id = ensureCategory(depth2);
            Long d3Id = ensureCategory(depth3);

            insertClosure(d1Id, d1Id, 0);
            insertClosure(d1Id, d2Id, 1);
            insertClosure(d1Id, d3Id, 2);
            insertClosure(d2Id, d2Id, 0);
            insertClosure(d2Id, d3Id, 1);
            insertClosure(d3Id, d3Id, 0);

            Long dummyStorageId = getOrCreateDummyStorageId();

            documentRepository.save(Document.builder()
                    .categoryId(d3Id)
                    .title(depth3)
                    .uploaderName("시스템")
                    .createdAt(LocalDateTime.now())
                    .storageId(dummyStorageId)
                    .build());
        }
    }

    /**
     * 주어진 카테고리 이름이 존재하면 ID 반환, 없으면 생성
     */
    private Long ensureCategory(String name) {
        if (categoryCache.containsKey(name)) return categoryCache.get(name);

        Category cat = categoryRepository.findByName(name).orElseGet(() ->
                categoryRepository.save(Category.builder().name(name).build()));

        categoryCache.put(name, cat.getId());
        return cat.getId();
    }

    /**
     * 조상-자손 관계 삽입 (중복 방지)
     */
    private void insertClosure(Long ancestor, Long descendant, int depth) {
        String key = ancestor + "-" + descendant;

        if (closureCache.contains(key)) return;

        if (!closureRepository.existsByAncestorAndDescendant(ancestor, descendant)) {
            closureRepository.save(new CategoryClosure(ancestor, descendant, depth));
        }

        closureCache.add(key);
    }

    private Long getOrCreateDummyStorageId() {
        return storageRepository.findByPath("dummy-path")
                .map(Storage::getId)
                .orElseGet(() -> {
                    Storage storage = Storage.builder()
                            .path("dummy-path")
                            .size(0)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return storageRepository.save(storage).getId();
                });
    }
}