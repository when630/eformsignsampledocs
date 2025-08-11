package com.eformsign.sample.service;

import com.eformsign.sample.entity.*;
import com.eformsign.sample.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.text.Collator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // ───── 정렬 유틸: 숫자 접두사(예: "1. ...") 우선 + 한글 자연 정렬 ─────
    private static final Collator KO = Collator.getInstance(Locale.KOREAN);
    private static final Pattern LEADING_NUM = Pattern.compile("^\\s*(\\d+)\\s*[.)]?\\s*(.*)$");

    private static int compareNamesNatural(String a, String b) {
        Matcher ma = LEADING_NUM.matcher(a);
        Matcher mb = LEADING_NUM.matcher(b);

        boolean ha = ma.find();
        boolean hb = mb.find();

        if (ha && hb) {
            int na = Integer.parseInt(ma.group(1));
            int nb = Integer.parseInt(mb.group(1));
            if (na != nb) return Integer.compare(na, nb);
            return KO.compare(ma.group(2), mb.group(2));
        } else if (ha) {
            return -1; // 번호 있는 쪽 먼저
        } else if (hb) {
            return 1;
        } else {
            return KO.compare(a, b);
        }
    }

    private static final Comparator<File> FILE_NATURAL = (f1, f2) ->
            compareNamesNatural(f1.getName(), f2.getName());

    /**
     * 파일 경로를 읽어 Category/Closure/Storage/Document 자동 생성
     */
    @Transactional
    public void initializeFromFolder(String rootPath) {
        File rootDir = new File(rootPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new IllegalArgumentException("루트 디렉토리가 존재하지 않음: " + rootPath);
        }

        // depth1 디렉터리 정렬
        File[] d1Arr = Optional.ofNullable(rootDir.listFiles(File::isDirectory)).orElse(new File[0]);
        Arrays.sort(d1Arr, FILE_NATURAL);

        for (File depth1Dir : d1Arr) {
            String depth1 = depth1Dir.getName();
            Long d1Id = ensureCategory(depth1, 0L); // 루트는 parentId = 0L
            insertClosure(d1Id, d1Id, 0);

            // depth2 디렉터리 정렬
            File[] d2Arr = Optional.ofNullable(depth1Dir.listFiles(File::isDirectory)).orElse(new File[0]);
            Arrays.sort(d2Arr, FILE_NATURAL);

            for (File depth2Dir : d2Arr) {
                String depth2 = depth2Dir.getName();
                Long d2Id = ensureCategory(depth2, d1Id);
                insertClosure(d1Id, d2Id, 1);
                insertClosure(d2Id, d2Id, 0);

                // 파일 정렬
                File[] files = Optional.ofNullable(depth2Dir.listFiles(File::isFile)).orElse(new File[0]);
                Arrays.sort(files, FILE_NATURAL);

                for (File file : files) {
                    String fileName = file.getName();
                    if (fileName.equals(".DS_Store") || fileName.startsWith("~$")) {
                        log.info("무시된 파일: {}", file.getPath());
                        continue;
                    }

                    String depth3 = removeExtension(fileName);
                    Long d3Id = ensureCategory(depth3, d2Id);
                    insertClosure(d1Id, d3Id, 2);
                    insertClosure(d2Id, d3Id, 1);
                    insertClosure(d3Id, d3Id, 0);

                    log.info("저장할 문서: {}/{}/{}", depth1, depth2, fileName);

                    // Storage 저장
                    Storage storage = Storage.builder()
                            .path(file.getPath())
                            .size(file.length())
                            .createdAt(LocalDateTime.now())
                            .build();
                    Storage savedStorage = storageRepository.save(storage);

                    // Document 저장
                    documentRepository.save(Document.builder()
                            .categoryId(d3Id) // (장기적: 연관관계로 교체 권장)
                            .title(depth3)
                            .uploaderName("시스템")
                            .createdAt(LocalDateTime.now())
                            .storageId(savedStorage.getId())
                            .build());
                }
            }
        }
    }

    private Long ensureCategory(String name, Long parentId) {
        String key = parentId + ">" + name;
        if (categoryCache.containsKey(key)) return categoryCache.get(key);

        List<Category> candidates = categoryRepository.findAllByName(name);

        // parentId 아래에 실제로 매달린 것만 필터
        Category matched = candidates.stream()
                .filter(cat -> parentId == 0L ||
                        closureRepository.existsByAncestorAndDescendant(parentId, cat.getId()))
                .findFirst()
                .orElse(null);

        if (matched == null) {
            // 없으면 생성 → 즉시 flush로 FK 대상 보장
            Category created = categoryRepository.saveAndFlush(
                    Category.builder().name(name).build()
            );
            matched = created;
        }

        categoryCache.put(key, matched.getId());
        return matched.getId();
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