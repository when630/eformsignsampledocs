package com.eformsign.sample.repository;

import com.eformsign.sample.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // 기존 메서드 유지
    List<Document> findByCategoryId(Long categoryId);
    Optional<Document> findById(Long id);
    List<Document> findByCategoryIdIn(List<Long> ids);

    //    제목 "완전 일치"(대소문자 무시)
    Page<Document> findByTitleEqualsIgnoreCase(String q, Pageable pageable);

    //    제목 "부분 일치"(대소문자 무시) — 단어 경계 미지원 DB일 때도 안전
    Page<Document> findByTitleContainingIgnoreCase(String q, Pageable pageable);

    //    제목 "단어 경계" 일치 (MySQL 8+ REGEXP)
    //    일부 환경에서 REGEXP 단어경계가 안 먹으면 Controller/Service에서
    //    findByTitleContainingIgnoreCase(...)로 fallback 하세요.
    @Query(value = """
            SELECT * FROM document d
            WHERE LOWER(d.title) REGEXP CONCAT('[[:<:]]', LOWER(:q), '[[:>:]]')
            ORDER BY d.title ASC
            """,
            countQuery = """
            SELECT COUNT(*) FROM document d
            WHERE LOWER(d.title) REGEXP CONCAT('[[:<:]]', LOWER(:q), '[[:>:]]')
            """,
            nativeQuery = true)
    Page<Document> findByTitleWord(@Param("q") String q, Pageable pageable);
}