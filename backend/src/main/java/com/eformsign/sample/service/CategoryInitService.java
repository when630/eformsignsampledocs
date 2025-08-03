package com.eformsign.sample.service;

import com.eformsign.sample.entity.*;
import com.eformsign.sample.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryInitService {

    private final CategoryRepository categoryRepository;
    private final CategoryClosureRepository closureRepository;
    private final DocumentRepository documentRepository;
    private final StorageRepository storageRepository;

    private final Map<String, Long> categoryCache = new HashMap<>();
    private final Set<String> closureCache = new HashSet<>();

    /**
     * 파일 경로를 읽어 Category/Closure/Storage/Document 자동 생성
     */
    @Transactional
    public void initializeFromFolder(String rootPath) {
        File rootDir = new File(rootPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new IllegalArgumentException("루트 디렉토리가 존재하지 않음: " + rootPath);
        }

        for (File depth1Dir : Objects.requireNonNull(rootDir.listFiles(File::isDirectory))) {
            String depth1 = depth1Dir.getName();
            Long d1Id = ensureCategory(depth1);
            insertClosure(d1Id, d1Id, 0);

            for (File depth2Dir : Objects.requireNonNull(depth1Dir.listFiles(File::isDirectory))) {
                String depth2 = depth2Dir.getName();
                Long d2Id = ensureCategory(depth2);
                insertClosure(d1Id, d2Id, 1);
                insertClosure(d2Id, d2Id, 0);

                File[] files = depth2Dir.listFiles();
                if (files == null) continue;

                for (File file : files) {
                    String fileName = file.getName();
                    if (!file.isFile()) continue;
                    if (fileName.equals(".DS_Store") || fileName.startsWith("~$")) {
                        log.info("⛔ 무시된 파일: {}", file.getPath());
                        continue;
                    }

                    String depth3 = removeExtension(fileName);
                    Long d3Id = ensureCategory(depth3);

                    insertClosure(d1Id, d3Id, 2);
                    insertClosure(d2Id, d3Id, 1);
                    insertClosure(d3Id, d3Id, 0);

                    log.info("📄 저장할 문서: {}/{}/{}", depth1, depth2, fileName);

                    // Storage 저장
                    Storage storage = Storage.builder()
                            .path(file.getPath())
                            .size(file.length())
                            .createdAt(LocalDateTime.now())
                            .build();
                    Storage savedStorage = storageRepository.save(storage);

                    // Document 저장
                    documentRepository.save(Document.builder()
                            .categoryId(d3Id)
                            .title(depth3)
                            .uploaderName("시스템")
                            .createdAt(LocalDateTime.now())
                            .storageId(savedStorage.getId())
                            .build());
                }
            }
        }
    }

    private Long ensureCategory(String name) {
        if (categoryCache.containsKey(name)) return categoryCache.get(name);

        Category cat = categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(Category.builder().name(name).build()));
        categoryCache.put(name, cat.getId());
        return cat.getId();
    }

    private void insertClosure(Long ancestor, Long descendant, int depth) {
        String key = ancestor + "-" + descendant;
        if (closureCache.contains(key)) return;

        if (!closureRepository.existsByAncestorAndDescendant(ancestor, descendant)) {
            closureRepository.save(new CategoryClosure(ancestor, descendant, depth));
        }

        closureCache.add(key);
    }

    private String removeExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }
}